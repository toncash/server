package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.model.OrderStatus;
import org.springframework.data.geo.Point;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDto, long personId);

    OrderDTO getOrder(String id);

    Iterable<OrderDTO> getOrders();
    Iterable<OrderDTO> getOrdersByPersonId(long personId);

    Iterable<OrderDTO> getOrdersByLocation(Point point, double distance);

    void deleteOrder(String id);

    OrderDTO changeOrder(String id, OrderDTO orderDTO);

    void changeOrderStatus(String orderId, long personId, OrderStatus status);

    OrderDTO orderRequest(String orderId, long personId, OrderStatus status);

    void rejectOrder(String orderId, long personId);
}
