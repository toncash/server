package com.hackaton.hackatonchangeapp.repo;

import com.hackaton.hackatonchangeapp.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends CrudRepository<Order, String> {
}
