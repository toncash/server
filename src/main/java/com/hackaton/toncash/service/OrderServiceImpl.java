package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.*;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.*;
import com.hackaton.toncash.repo.OrderRepo;
import com.hackaton.toncash.repo.PersonRepo;
import com.hackaton.toncash.tgbot.TonBotService;
import com.hackaton.toncash.tgbot.TonCashBot;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
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

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.hackaton.toncash.service.CommonMethods.mapToPersonDTO;
import static com.hackaton.toncash.service.CommonMethods.updateEntity;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepository;
    private final PersonService personService;
    private final PersonRepo personRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
    private final TonCashBot bot;


    @Override
    public PersonOrderDTO createOrder(OrderDTO orderDto) {

        Order order = modelMapper.map(orderDto, Order.class);
        order.setDeals(new ArrayList<>());
        order.setLocalDateTime(LocalDateTime.now());
        long personId = orderDto.getOwnerId();
        orderRepository.save(order);

        personService.addOrderToPerson(personId, order.getId());

        String message = "You created the order for " + order.getOrderType() + " with " + order.getAmount() + "TON";
        TonBotService.sendNotification(bot,Long.toString(personId), message);

        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));

    }

    @Override
    public PersonOrderDTO getOrder(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(order, OrderDTO.class));

    }

    @Override
    public Iterable<PersonOrderDTO> getOrders() {
        return StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .map(order -> mapOrderDTOtoPersonOrderDTO(modelMapper.map(order, OrderDTO.class)))
                .collect(Collectors.toList());
    }

    public Iterable<OrderDTO> getOrdersByPersonId(long personId) {
        PersonDTO personDTO = personService.getPerson(personId);

        return StreamSupport.stream(orderRepository.findAllById(personDTO.getCurrentOrders()).spliterator(), false)
                .map(order -> modelMapper.map(order, OrderDTO.class))
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

        List<Long> personIds =  orders.stream().map(Order::getOwnerId).collect(Collectors.toList());
        List<Person> persons = StreamSupport.stream(personRepository.findAllById(personIds).spliterator(), false).collect(Collectors.toList());

        Map<Long, Person> personMap = persons.stream().collect(Collectors.toMap(Person::getId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
                    PersonDTO personDTO = mapToPersonDTO(personMap.get(order.getOwnerId()));
                    return new PersonOrderDTO(personDTO, orderDTO);
                })
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
        String message = "You delete the order for " + order.getOrderType() + " with " + order.getAmount() + "TON";
        TonBotService.sendNotification(bot,Long.toString(order.getOwnerId()), message);

        orderRepository.delete(order);
    }

    @Override
    public PersonOrderDTO changeOrder(String id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(updateEntity(order, orderDTO)), OrderDTO.class));

    }


    @Override
    public PersonOrderDTO changeOrderStatus(String orderId, long personId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        if (status.equals(OrderStatus.PENDING)) {
//            takeOrder(personId, order, true);
        }
//        if (status.equals(OrderStatus.BAD)) {
//            rejectOrder(orderId, personId);
//        }
       return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));
    }


    private PersonOrderDTO mapOrderDTOtoPersonOrderDTO(OrderDTO orderDTO) {
        PersonDTO personDTO = personService.getPerson(orderDTO.getOwnerId());
        return new PersonOrderDTO(personDTO, orderDTO);
    }





}
