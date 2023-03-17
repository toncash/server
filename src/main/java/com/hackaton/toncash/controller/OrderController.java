package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("api/v1/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping
    public PersonOrderDTO createOrder(@RequestBody OrderDTO orderDto) {
        return changeOrder(orderService.createOrder(orderDto));
    }

    @GetMapping("{id}")
    public PersonOrderDTO getOrder(@PathVariable String id) {
        return changeOrder(orderService.getOrder(id));

    }

    @DeleteMapping("{id}")
    public void deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestParam(defaultValue = "") Long personId) {
        if (personId == null) {
            Iterable<PersonOrderDTO> orders = orderService.getOrders();
            List<PersonOrderDTO> updateOrders = changeOrders(orders);
            return ResponseEntity.ok(updateOrders);
        } else {
            Iterable<OrderDTO> orders = orderService.getOrdersByPersonId(personId);
            return ResponseEntity.ok( changeOrderDTOs(orders));
        }
    }


    @PutMapping("{orderId}")
    public PersonOrderDTO changeOrderStatus(@PathVariable String orderId, @RequestParam long personId, @RequestParam OrderStatus status) {
        return changeOrder(orderService.changeOrderStatus(orderId, personId, status));
    }


    @GetMapping("/location")
    public Iterable<PersonOrderDTO> getOrdersByLocation(@RequestParam String location, double distance) {
        String[] coords = location.split(",");
        double latitude = Double.parseDouble(coords[0]);
        double longitude = Double.parseDouble(coords[1]);
        Iterable<PersonOrderDTO> ordersByLocation = orderService.getOrdersByLocation(new Point(longitude, latitude), distance);
        return changeOrders(ordersByLocation);
    }

    private List<OrderDTO> changeOrderDTOs(Iterable<OrderDTO> orders) {
        return StreamSupport.stream(orders.spliterator(), false)
                .peek(order -> {
                            Point location = order.getLocation();
                            double x = location.getX();
                            double y = location.getY();
                            Point newLocation = new Point(y, x);
                            order.setLocation(newLocation);
                        }
                )
                .collect(Collectors.toList());
    }

    private PersonOrderDTO changeOrder(PersonOrderDTO personOrderDTO) {
        OrderDTO order = personOrderDTO.getOrder();
        Point location = order.getLocation();
        double x = location.getX();
        double y = location.getY();
        Point newLocation = new Point(y, x);
        order.setLocation(newLocation);
        return new PersonOrderDTO(personOrderDTO.getPerson(), order);
    }

    private List<PersonOrderDTO> changeOrders(Iterable<PersonOrderDTO> orders) {
        return StreamSupport.stream(orders.spliterator(), false)
                .map(this::changeOrder)
                .collect(Collectors.toList());
    }
}
