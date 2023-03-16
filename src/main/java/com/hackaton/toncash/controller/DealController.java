package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
import com.hackaton.toncash.service.DealService;
import com.hackaton.toncash.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/deals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping()
    public PersonDealDTO createDeal(@RequestBody DealDTO dealDTO, @RequestParam Long clientId) {
        return dealService.createDeal(dealDTO, clientId);
    }

    @PostMapping("{dealId}")
    public PersonDealDTO acceptDeal(@PathVariable String dealId) {
        return dealService.acceptDeal(dealId);
    }

    @PutMapping("{dealId}")
    public ResponseEntity<PersonDealDTO> denyOrUpdateDeal(@PathVariable String dealId, @Valid @RequestBody(required = false) DealDTO dealDTO) {
        if (dealDTO == null) {
            dealService.denyDeal(dealId);
            return ResponseEntity.noContent().build();
        } else {

            PersonDealDTO updatedDeal = dealService.updateDeal(dealId, dealDTO);
            return ResponseEntity.ok(updatedDeal);
        }

    }

    @DeleteMapping("{dealId}")
    public void deleteDeal(@PathVariable String dealId) {
        dealService.deleteDeal(dealId);
    }

    @GetMapping("{dealId}")
    public PersonDealDTO getOrderDeal(@PathVariable String dealId) {
        System.out.println(dealId);
        return dealService.getOrderDeal(dealId);
    }

    @GetMapping()
    public Iterable<PersonDealDTO> getOrderDeals(@RequestParam String orderId) {
        return dealService.getOrderDeals(orderId);
    }


}
