package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.model.Order;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.OrderType;
import com.hackaton.toncash.repo.OrderRepo;
import com.hackaton.toncash.tgbot.TonCashBot;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
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

    private final TonCashBot bot;

    @Override
    public OrderDTO createOrder(OrderDTO orderDto, long personId) {

        Order order = modelMapper.map(orderDto, Order.class);
        order.setLocalDateTime(LocalDateTime.now());
        orderRepository.save(order);
        if (orderDto.getOrderType().equals(OrderType.BUY)) {
            orderDto.setBuyerId(personId);
        } else {
            orderDto.setSellerId(personId);
        }
        personService.addOrderToPerson(personId, order.getId());

        SendMessage message = new SendMessage(Long.toString(personId), "You created order for " + order.getOrderType() + " with " + order.getAmount() + "TON");
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return modelMapper.map(orderRepository.save(order), OrderDTO.class);
    }

    @Override
    public OrderDTO getOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public Iterable<OrderDTO> getOrders() {
        return StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .map(o -> modelMapper.map(o, OrderDTO.class))
                .collect(Collectors.toList());
    }

    public Iterable<OrderDTO> getOrdersByPersonId(long personId) {
        Set<String> currentOrders = personService.getPerson(personId).getCurrentOrders();
        return StreamSupport.stream(orderRepository.findAllById(currentOrders).spliterator(), false)
                .map(o -> modelMapper.map(o, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<OrderDTO> getOrdersByLocation(Point point, double distance) {
//        mongoTemplate.indexOps(Order.class)
//                .ensureIndex(new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE));
        Query query = new Query();

        query.addCriteria(new Criteria().andOperator(
                Criteria.where("orderStatus").is(OrderStatus.CURRENT),
                Criteria.where("location").nearSphere(point).maxDistance(distance)));
        List<Order> orders = mongoTemplate.find(query, Order.class);

        return orders.stream()
                .map(o -> modelMapper.map(o, OrderDTO.class))
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
    public OrderDTO changeOrder(String id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        BeanUtils.copyProperties(orderDTO, order, CommonMethods.getNullPropertyNames(orderDTO));

        return modelMapper.map(orderRepository.save(order), OrderDTO.class);
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

    private void takeOrder(long personId, Order order) {
        if (order.getOrderType().equals(OrderType.BUY)) {
            order.setSellerId(personId);
        } else {
            order.setBuyerId(personId);
        }
    }

    @Override
    public void rejectOrder(String orderId, long personId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        //FIXME conditions of reject
        order.setOrderStatus(OrderStatus.BAD);
        personService.changeStatusOrderFromPerson(personId, orderId, OrderStatus.BAD);
        orderRepository.save(order);
    }


}
