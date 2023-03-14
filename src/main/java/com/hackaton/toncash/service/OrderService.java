package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.model.OrderStatus;
import org.springframework.data.geo.Point;

public interface OrderService {
    PersonOrderDTO createOrder(OrderDTO orderDto);

    PersonOrderDTO getOrder(String id);

    Iterable<PersonOrderDTO> getOrders();

    Iterable<PersonOrderDTO> getOrdersByPersonId(long personId);

    Iterable<PersonOrderDTO> getOrdersByLocation(Point point, double distance);

    void deleteOrder(String id);

    PersonOrderDTO changeOrder(String id, OrderDTO orderDTO);

    void changeOrderStatus(String orderId, long personId, OrderStatus status);

    PersonDealDTO createDeal(DealDTO dealDTO, Long clientId);

    PersonDealDTO getOrderDeal(String orderId, String dealId);

    Iterable<PersonDealDTO> getOrderDeals(String orderId);

    PersonDealDTO acceptDeal(String orderId, String dealId);

    void denyDeal(String orderId, String dealId);
}
