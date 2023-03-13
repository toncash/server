package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.model.*;
import com.hackaton.toncash.repo.OrderRepo;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepository;
    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;
//    private final TonCashBot bot;


    @Override
    public PersonOrderDTO createOrder(OrderDTO orderDto) {

        Order order = modelMapper.map(orderDto, Order.class);
        order.setDeals(new ArrayList<>());
        order.setLocalDateTime(LocalDateTime.now());
        long personId = orderDto.getOwnerId();
        orderRepository.save(order);

        personService.addOrderToPerson(personId, order.getId());

        String message = "You created order for " + order.getOrderType() + " with " + order.getAmount() + "TON";
//        TonBotService.sendNotification(bot,Long.toString(personId), message);

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

        return mapOrderDTOtoPersonOrderDTO(modelMapper.map(orderRepository.save(order), OrderDTO.class));

    }


    @Override
    public void changeOrderStatus(String orderId, long personId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        if (status.equals(OrderStatus.PENDING)) {
//            takeOrder(personId, order, true);
        }
        if (status.equals(OrderStatus.BAD)) {
            rejectOrder(orderId, personId);
        }
        orderRepository.save(order);
    }


    public void denyOrder(long personId, String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        long ownerId;
//        if (order.getOrderType().equals(OrderType.BUY)) {
//            ownerId = order.getBuyerId();
//            order.setSellerId(personId);
//        } else {
//            ownerId = order.getSellerId();
//            order.setBuyerId(personId);
//        }
        String clientUsername = personService.getPerson(personId).getUsername();
        String ownerMessage = "You denied the offer from the client @" + clientUsername + " for order " + order.getOrderType() + " with " + order.getAmount() + "TON";
        String clientMessage = "The owner of the order denied your offer";
//        TonBotService.sendNotification(bot,Long.toString(ownerId), ownerMessage);
//        TonBotService.sendNotification(bot,Long.toString(personId), clientMessage);
    }

    @Override
    public void rejectOrder(String orderId, long personId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        //FIXME conditions of reject
        order.setOrderStatus(OrderStatus.BAD);
        personService.changeStatusOrderFromPerson(personId, orderId, OrderStatus.BAD);
        orderRepository.save(order);
    }


    @Override
    public DealDTO createDeal(DealDTO dealDTO, Long clientId) {
        Order order = orderRepository.findById(dealDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException(dealDTO.getOrderId()));
        Deal deal = madeDeal(order, dealDTO, clientId);

        dealRequest(order, clientId, deal.getId());

        order.getDeals().add(deal);
        orderRepository.save(order);
        return modelMapper.map(deal, DealDTO.class);

    }

    private Deal madeDeal(Order order, DealDTO dealDTO, Long clientId) {
        Deal deal = Deal.builder()
                .id(new ObjectId().toString())
                .localDateTime(LocalDateTime.now())
                .dealStatus(DealStatus.CURRENT)
                .addressContract(dealDTO.getAddressContract())
                .amount(dealDTO.getAmount())
                .orderId(dealDTO.getOrderId())
                .build();
        if (order.getOrderType().equals(OrderType.BUY)) {
            deal.setBuyerId(order.getOwnerId());
            deal.setSellerId(clientId);

        } else {
            deal.setSellerId(order.getOwnerId());
            deal.setBuyerId(clientId);
        }
        if (dealDTO.getAmount() > order.getAmount()) {
            throw new BadRequestException("Your amount greater than amount of order");
        }
        return deal;
    }

    private void dealRequest(Order order, long clientId, String dealId) {
        String clientUsername = personService.getPerson(clientId).getUsername();
        String message = "You have an offer from @" + clientUsername + " by your order " + order.getOrderType() + " " + order.getAmount() + "TON";
//        TonBotService.sendOfferDealNotification(bot, Long.toString(order.getOwnerId()), message, dealId, clientId);
    }

    public void acceptDeal(long clientId, String dealId, Long ownerOrderId, Long chatId, Integer messageId) {
        System.out.println("accept deal");
        Order order = orderRepository.findByDealsId(dealId);
        Deal deal = findDealInOrder(dealId, order);
        float amount = order.getAmount();
//        order.setAmount(amount-deal.getAmount());
        deal.setDealStatus(DealStatus.PENDING);
//
        String clientUsername = personService.getPerson(clientId).getUsername();
        String messageOwner = "You accept a client @" + clientUsername + " by the order " + order.getOrderType() + " " + order.getAmount() + "TON";
        String orderTypeForClient = "BUY";
        if (order.getOrderType().equals(OrderType.BUY)) {
            orderTypeForClient = "SELL";
        }
        String messageClient = "Your offer has been confirmed @" + clientUsername + " by the deal " + orderTypeForClient + " with " + deal.getAmount() + "TON";
//        TonBotService.sendNotification(bot, Long.toString(clientId), messageClient);
//        TonBotService.sendEditMassage(bot, chatId, messageId, messageOwner, order.getId(), dealId, clientId);
    }

    @Override
    public DealDTO getDeal(String orderId, String dealId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        Deal deal = findDealInOrder(dealId, order);
        return modelMapper.map(deal, DealDTO.class);

    }

    private Deal findDealInOrder(String dealId, Order order) {
        Deal fakeDeal = new Deal();
        fakeDeal.setId(dealId);
        int index = order.getDeals().indexOf(fakeDeal);
        if (index < 0) {
            throw new BadRequestException("Deal with id - " + dealId + " doesn't exist");
        }
        return order.getDeals().get(index);
    }

    @Override
    public Iterable<DealDTO> getDeals(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return order.getDeals().stream()
                .map(deal -> modelMapper.map(deal, DealDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<DealDTO> getDealsByPersonId(Long personId) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("deals.buyerId").is(personId));
//        query.addCriteria(Criteria.where("deals.sellerId").is(personId));
//        List<Deal> deals = mongoTemplate.find(query, Order.class);
        return orderRepository.findByDealsBuyerIdOrDealsSellerId(personId).stream()
                .map(deal -> modelMapper.map(deal, DealDTO.class))
                .collect(Collectors.toList());
    }

    private PersonOrderDTO mapOrderDTOtoPersonOrderDTO(OrderDTO orderDTO) {
        PersonDTO personDTO = personService.getPerson(orderDTO.getOwnerId());
        return new PersonOrderDTO(personDTO, orderDTO);
    }


}
