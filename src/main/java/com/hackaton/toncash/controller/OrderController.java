package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@CrossOrigin
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping
    public OrderDTO createOrder(@RequestBody OrderDTO orderDto) {
        return orderService.createOrder(orderDto);
    }

    @GetMapping("{id}")
    public OrderDTO getOrder(@PathVariable String id) {
        return orderService.getOrder(id);
    }

    @DeleteMapping("{id}")
    public void deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
    }

    @GetMapping
    public Iterable<OrderDTO> getOrders() {
        return orderService.getOrders();
    }

    @PutMapping ("{orderId}/person/{personId}")
    public void takeOrder(@PathVariable String orderId, @PathVariable long personId) {
        orderService.takeOrder(orderId, personId);
    }

    @PostMapping ("{orderId}/person/{personId}")
    public void rejectOrder(@PathVariable String orderId, @PathVariable long personId) {
        orderService.rejectOrder(orderId, personId);
    }
    @GetMapping("/location")
    public Iterable<OrderDTO> getOrdersByLocation(@RequestParam String location, double distance) {
        String[] coords = location.split(",");
        double latitude = Double.parseDouble(coords[0]);
        double longitude = Double.parseDouble(coords[1]);
        return orderService.getOrdersByLocation(new Point(latitude, longitude), distance);
    }


}
