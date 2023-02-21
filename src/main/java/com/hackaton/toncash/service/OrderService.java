package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import org.springframework.data.geo.Point;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDto);

    OrderDTO getOrder(String id);

    Iterable<OrderDTO> getOrders();

    Iterable<OrderDTO> getOrdersByLocation(Point point, double distance);

    void deleteOrder(String id);

    OrderDTO changeOrder(String id, OrderDTO orderDTO);

    void takeOrder(String orderId, long personId);

    void rejectOrder(String orderId, long personId);
}
