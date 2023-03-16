package com.hackaton.toncash.repo;

import com.hackaton.toncash.model.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
    Optional<Person> findByUsername(String username);
    Optional<Person> findByTelegramId(Long telegramId);
    Optional<Person> findByCurrentDealsContains(String dealId);
}
