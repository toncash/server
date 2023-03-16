package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
import com.hackaton.toncash.service.DealService;
import lombok.AllArgsConstructor;
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

    @DeleteMapping("deals/{dealId}")
    public void createDeal(@PathVariable String dealId) {
         dealService.deleteDeal(dealId);
    }

    @PostMapping("deals/{dealId}")
    public PersonDealDTO acceptDeal(@PathVariable String dealId) {
       return dealService.acceptDeal(dealId);
    }

    @PutMapping ("/deals/{dealId}")
    public void denyDeal(@PathVariable String dealId) {
        dealService.denyDeal( dealId);
    }
    @GetMapping("deals/{dealId}")
    public PersonDealDTO getDeal(@PathVariable String dealId) {
        return dealService.getDeal( dealId);
    }

    @GetMapping("{orderId}/deals")
    public Iterable<PersonDealDTO> getOrderDeals(@PathVariable String orderId) {
        return dealService.getOrderDeals(orderId);
    }


}
