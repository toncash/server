package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
import com.hackaton.toncash.dto.PersonOrderDTO;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.service.DealService;
import com.hackaton.toncash.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping("deals")
    public PersonDealDTO createDeal(@RequestBody DealDTO dealDTO, @RequestParam Long clientId) {
        return dealService.createDeal(dealDTO, clientId);
    }

    @PostMapping("{orderId}/deals/{dealId}")
    public PersonDealDTO acceptDeal(@PathVariable String orderId, @PathVariable String dealId) {
       return dealService.acceptDeal(orderId, dealId);
    }

    @PutMapping ("{orderId}/deals/{dealId}")
    public void denyDeal(@PathVariable String orderId, @PathVariable String dealId) {
        dealService.denyDeal(orderId, dealId);
    }
    @GetMapping("{orderId}/deals/{dealId}")
    public PersonDealDTO getOrderDeal(@PathVariable String orderId, @PathVariable String dealId) {
        return dealService.getOrderDeal(orderId, dealId);
    }

    @GetMapping("{orderId}/deals")
    public Iterable<PersonDealDTO> getOrderDeals(@PathVariable String orderId) {
        return dealService.getOrderDeals(orderId);
    }


}
