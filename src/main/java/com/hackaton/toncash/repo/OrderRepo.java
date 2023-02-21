package com.hackaton.toncash.repo;

import com.hackaton.toncash.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends CrudRepository<Order, String> {
}
