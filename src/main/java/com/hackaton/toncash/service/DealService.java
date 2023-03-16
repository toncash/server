package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDealDTO;

public interface DealService {

    PersonDealDTO createDeal(DealDTO dealDTO, Long clientId);

    PersonDealDTO getDeal(String dealId);

    Iterable<PersonDealDTO> getOrderDeals(String orderId);

    PersonDealDTO acceptDeal(String dealId);

    void denyDeal(String dealId);

    void deleteDeal(String dealId);
}
