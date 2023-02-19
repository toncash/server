package com.hackaton.hackatonchangeapp.repo;

import com.hackaton.hackatonchangeapp.model.Order;
import com.hackaton.hackatonchangeapp.model.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepo extends CrudRepository<Person, String> {
    Optional<Person> findByUsername(String username);
}
