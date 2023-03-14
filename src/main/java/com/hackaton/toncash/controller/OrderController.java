package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
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


    @GetMapping("/location")
    public Iterable<PersonOrderDTO> getOrdersByLocation(@RequestParam String location, double distance) {
        String[] coords = location.split(",");
        double latitude = Double.parseDouble(coords[0]);
        double longitude = Double.parseDouble(coords[1]);
        return orderService.getOrdersByLocation(new Point(latitude, longitude), distance);
    }


    @PostMapping("deals")
    public PersonDealDTO createDeal(@RequestBody DealDTO dealDTO, @RequestParam Long clientId) {
        return orderService.createDeal(dealDTO, clientId);
    }

    @PostMapping("{orderId}/deals/{dealId}")
    public PersonDealDTO acceptDeal(@PathVariable String orderId, @PathVariable String dealId) {
       return orderService.acceptDeal(orderId, dealId);
    }

    @PutMapping ("{orderId}/deals/{dealId}")
    public void denyDeal(@PathVariable String orderId, @PathVariable String dealId) {
        orderService.denyDeal(orderId, dealId);
    }
    @GetMapping("{orderId}/deals/{dealId}")
    public PersonDealDTO getOrderDeal(@PathVariable String orderId, @PathVariable String dealId) {
        return orderService.getOrderDeal(orderId, dealId);
    }

    @GetMapping("{orderId}/deals")
    public Iterable<PersonDealDTO> getOrderDeals(@PathVariable String orderId) {
        return orderService.getOrderDeals(orderId);
    }


}
