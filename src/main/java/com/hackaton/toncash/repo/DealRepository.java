package com.hackaton.toncash.repo;

import com.hackaton.toncash.model.Deal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends CrudRepository<Deal, String> {

}
