package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.*;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.*;
import com.hackaton.toncash.repo.DealRepository;
import com.hackaton.toncash.repo.OrderRepository;
import com.hackaton.toncash.repo.PersonRepository;
import lombok.AllArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.hackaton.toncash.service.CommonMethods.getPersonOrderDTO;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PersonRepository personRepository;
    private final DealRepository dealRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
//    private final TonCashBot bot;


    @Override
    public PersonOrderDTO createOrder(OrderDTO orderDto) {

        Order order = modelMapper.map(orderDto, Order.class);
        order.setDeals(new HashSet<>());
        order.setLocalDateTime(LocalDateTime.now());
        orderRepository.save(order);

        Person person = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));
        person.getCurrentOrders().add(order.getId());
        personRepository.save(person);

        long personTelegramId = person.getTelegramId();
        String message = "You created order for " + order.getOrderType() + " with " + order.getAmount() + "TON";
//        TonBotService.sendNotification(bot,Long.toString(personTelegramId), message);

        OrderDTO orderDTO = modelMapper.map(orderRepository.save(order), OrderDTO.class);

        return getPersonOrderDTO(person, orderDTO, modelMapper);

    }


    @Override
    public PersonOrderDTO getOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        Person person = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        return getPersonOrderDTO(person, orderDTO, modelMapper);

    }

    @Override
    public Iterable<PersonOrderDTO> getOrders() {
        return StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .map(this::getPersonOrderDTOByOrder)
                .collect(Collectors.toList());
    }


    public Iterable<PersonOrderDTO> getOrdersByPersonId(long personId) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new UserNotFoundException(personId));

        Set<String> currentOrders = person.getCurrentOrders();
        return StreamSupport.stream(orderRepository.findAllById(currentOrders).spliterator(), false)
                .map(order -> getPersonOrderDTO(
                        person,
                        modelMapper.map(order, OrderDTO.class),
                        modelMapper))
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
                .map(this::getPersonOrderDTOByOrder)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
//        if (order.getOrderType().equals(OrderType.BUY)) {
//            personService.removeOrderFromPerson(order.getBuyerId(), order.getId());
//        } else {
//            personService.removeOrderFromPerson(order.getSellerId(), order.getId());
//        }
        orderRepository.delete(order);
    }

    @Override
    public PersonOrderDTO changeOrder(String id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        BeanUtils.copyProperties(orderDTO, order, CommonMethods.getNullPropertyNames(orderDTO));

        return getPersonOrderDTOByOrder(order);


    }


    @Override
    public void changeOrderStatus(String orderId, long personId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        if (status.equals(OrderStatus.PENDING)) {
//            takeOrder(personId, order, true);
        }
//        if (status.equals(OrderStatus.BAD)) {
//            rejectOrder(orderId, personId);
//        }
        orderRepository.save(order);
    }

    private PersonOrderDTO getPersonOrderDTOByOrder(Order order) {
        Person person = personRepository.findById(order.getOwnerId()).orElseThrow(() -> new UserNotFoundException(order.getOwnerId()));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        return getPersonOrderDTO(person, orderDTO, modelMapper);
    }

//    private PersonOrderDTO getPersonOrderDTO(OrderDTO orderDTO, Person person) {
//        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
//        Iterable<Order> orders = orderRepository.findAllById(person.getCurrentOrders());
//        return CommonMethods.mapOrderDTOtoPersonOrderDTO(orderDTO, person, orders, deals, modelMapper);
//    }

}
