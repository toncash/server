package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
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
    public PersonOrderDTO getOrder(@PathVariable String id) {
        return orderService.getOrder(id);

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


    @PostMapping("deals")
    public DealDTO createDeal(@RequestBody DealDTO dealDTO, @RequestParam Long clientId) {
        return orderService.createDeal(dealDTO, clientId);
    }
    @GetMapping("{orderId}/deals/{dealId}")
    public DealDTO getDeal(@PathVariable String orderId, @PathVariable String dealId) {
        return orderService.getDeal(orderId, dealId);
    }

    @GetMapping("{orderId}/deals")
    public Iterable<DealDTO> getDeals(@PathVariable String orderId) {
        return orderService.getDeals(orderId);
    }

    @GetMapping("deals")
    public Iterable<DealDTO> getDealsByPersonId(@RequestParam Long personId) {
        return orderService.getDealsByPersonId(personId);
    }
}
