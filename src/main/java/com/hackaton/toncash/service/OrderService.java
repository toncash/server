package com.hackaton.toncash.service;

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

    OrderDTO orderRequest(String orderId, long personId, OrderStatus status);

    void rejectOrder(String orderId, long personId);
}
