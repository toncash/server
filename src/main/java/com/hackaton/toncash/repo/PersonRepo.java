package com.hackaton.toncash.repo;

import com.hackaton.toncash.model.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PersonRepo extends CrudRepository<Person, Long> {
    Optional<Person> findByUsername(String username);
}
