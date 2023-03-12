package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping
    public PersonOrderDTO createOrder(@RequestBody OrderDTO orderDto) {
        return orderService.createOrder(orderDto);
    }

    @GetMapping("{id}")
    public PersonOrderDTO getOrder(@PathVariable String id, @RequestParam(defaultValue = "") Long personId, @RequestParam(defaultValue = "") OrderStatus status) {
        System.out.println("123");
        if (personId == null && status == null) {
            return orderService.getOrder(id);
        } else {
            System.out.println("else");
            return orderService.orderRequest(id, personId, status);
        }
    }

    @DeleteMapping("{id}")
    public void deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
    }

    @GetMapping
    public Iterable<PersonOrderDTO> getOrders(@RequestParam(defaultValue = "") Long personId) {
        if (personId == null) {
            System.out.println(personId);
            return orderService.getOrders();
        } else {
            return orderService.getOrdersByPersonId(personId);
        }
    }


    @PutMapping("{orderId}")
    public void changeOrderStatus(@PathVariable String orderId, @RequestParam long personId, @RequestParam OrderStatus status) {
        orderService.changeOrderStatus(orderId, personId, status);
    }

    @PostMapping("{orderId}/person/{personId}")
    public void rejectOrder(@PathVariable String orderId, @PathVariable long personId) {
        orderService.rejectOrder(orderId, personId);
    }

    @GetMapping("/location")
    public Iterable<PersonOrderDTO> getOrdersByLocation(@RequestParam String location, double distance) {
        String[] coords = location.split(",");
        double latitude = Double.parseDouble(coords[0]);
        double longitude = Double.parseDouble(coords[1]);
        return orderService.getOrdersByLocation(new Point(latitude, longitude), distance);
    }


}
