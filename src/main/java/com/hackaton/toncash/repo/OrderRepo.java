package com.hackaton.toncash.repo;

import com.hackaton.toncash.model.Deal;
import com.hackaton.toncash.model.Order;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends CrudRepository<Order, String> {
    @Query(value = "{ 'deals.buyerId': { '$in': [?0] } , 'deals.sellerId': { '$in': [?0] } }", fields = "{ 'deals': 1 }")
    List<Deal> findByDealsBuyerIdOrDealsSellerId(Long personId);


    Order findByDealsId(String dealId);
}
