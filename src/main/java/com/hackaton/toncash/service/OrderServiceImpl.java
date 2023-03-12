package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.model.Order;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.OrderType;
import com.hackaton.toncash.repo.OrderRepo;
import lombok.AllArgsConstructor;
import com.hackaton.toncash.tgbot.TonCashBot;
import com.hackaton.toncash.tgbot.TonBotService;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepository;
    private final PersonService personService;

    //        private final PersonRepo personRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
    //    @Autowired
    private TonCashBot bot;


    @Override
    public PersonOrderDTO createOrder(OrderDTO orderDto) {

        Order order = modelMapper.map(orderDto, Order.class);
        order.setLocalDateTime(LocalDateTime.now());
        long personId = orderDto.getOwnerId();
        orderRepository.save(order);
        if (orderDto.getOrderType().equals(OrderType.BUY)) {
            order.setBuyerId(personId);
        } else {
            order.setSellerId(personId);
        }
        personService.addOrderToPerson(personId, order.getId());

        String message = "You created order for " + order.getOrderType() + " with " + order.getAmount() + "TON";
        TonBotService.sendNotification(bot,Long.toString(personId), message);

        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));
    }

    @Override
    public PersonOrderDTO getOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));

    }

    @Override
    public Iterable<PersonOrderDTO> getOrders() {
        return StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .map(order -> mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class)))
                .collect(Collectors.toList());
    }

    public Iterable<PersonOrderDTO> getOrdersByPersonId(long personId) {
        Set<String> currentOrders = personService.getPerson(personId).getCurrentOrders();
        return StreamSupport.stream(orderRepository.findAllById(currentOrders).spliterator(), false)
                .map(order -> mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class)))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<PersonOrderDTO> getOrdersByLocation(Point point, double requestDistance) {
//        mongoTemplate.indexOps(Order.class)
//                .ensureIndex(new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE));
        Query query = new Query();

        Distance distance = new Distance(requestDistance, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);

        query.addCriteria(Criteria.where("location").withinSphere(circle));
        query.addCriteria(Criteria.where("orderStatus").is(OrderStatus.CURRENT));
        List<Order> orders = mongoTemplate.find(query, Order.class);

        return orders.stream()
                .map(order -> mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class)))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getOrderType().equals(OrderType.BUY)) {
            personService.removeOrderFromPerson(order.getBuyerId(), order.getId());
        } else {
            personService.removeOrderFromPerson(order.getSellerId(), order.getId());
        }
        orderRepository.delete(order);
    }

    @Override
    public PersonOrderDTO changeOrder(String id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        BeanUtils.copyProperties(orderDTO, order, CommonMethods.getNullPropertyNames(orderDTO));

        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));

    }

    @Override
    public OrderDTO orderRequest(String orderId, long personId, OrderStatus status) {
        System.out.println("order id - " + orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        if (status.equals(OrderStatus.PENDING)) {
            takeOrder(personId, order, false);
        }
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public void changeOrderStatus(String orderId, long personId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        if (status.equals(OrderStatus.PENDING)) {
            takeOrder(personId, order);
        }
        if (status.equals(OrderStatus.BAD)){
            rejectOrder(orderId, personId);
        }
        orderRepository.save(order);
    }

    private void takeOrder(long personId, Order order, boolean flag) {
        long ownerId;
        if (order.getOrderType().equals(OrderType.BUY)) {
            ownerId = order.getBuyerId();
            order.setSellerId(personId);
        } else {
            ownerId = order.getSellerId();
            order.setBuyerId(personId);
        }
        String clientUsername = personService.getPerson(personId).getUsername();
        if (!flag) {
            String message = "You have a client @" + clientUsername + " for the order " + order.getOrderType() + " with " + order.getAmount() + "TON";
            TonBotService.sendNotificationWithApplyButton(bot,Long.toString(ownerId), message, order.getId(), personId);
        } else {
            String message = "You confirm the client @" + clientUsername + " for the order " + order.getOrderType() + " with " + order.getAmount() + "TON";
            TonBotService.sendNotification(bot,Long.toString(ownerId), message);
        }

    }

    public void denyOrder(long personId, String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        long ownerId;
        if (order.getOrderType().equals(OrderType.BUY)) {
            ownerId = order.getBuyerId();
            order.setSellerId(personId);
        } else {
            ownerId = order.getSellerId();
            order.setBuyerId(personId);
        }
        String clientUsername = personService.getPerson(personId).getUsername();
        String ownerMessage = "You denied the offer from the client @" + clientUsername + " for order " + order.getOrderType() + " with " + order.getAmount() + "TON";
        String clientMessage = "The owner of the order denied your offer";
        TonBotService.sendNotification(bot,Long.toString(ownerId), ownerMessage);
        TonBotService.sendNotification(bot,Long.toString(personId), clientMessage);
    }

    @Override
    public void rejectOrder(String orderId, long personId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        //FIXME conditions of reject
        order.setOrderStatus(OrderStatus.BAD);
        personService.changeStatusOrderFromPerson(personId, orderId, OrderStatus.BAD);
        orderRepository.save(order);
    }

    private PersonOrderDTO mapOrderDTOtoPersonOrderDTO(OrderDTO orderDTO) {
        PersonDTO personDTO = personService.getPerson(orderDTO.getOwnerId());
        return new PersonOrderDTO(personDTO, orderDTO);
    }


}
