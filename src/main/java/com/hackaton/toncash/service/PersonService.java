package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.model.OrderStatus;

public interface PersonService {
    void addPerson(PersonDTO personDto);

    PersonDTO firstInPerson(long id, PersonDTO personDTO);
    PersonDTO entrance(PersonDTO personDTO);

    PersonDTO getPerson(long id);

    Iterable<PersonDTO> getPersons();

    void deletePerson(long id);
    PersonDTO changePerson(long id, PersonDTO personDto);

    Iterable<DealDTO> getDealsByPersonId(Long personId);
    PersonDTO addOrderToPerson(long userId, String orderId);
    PersonDTO removeOrderFromPerson(long userId, String orderId);
    PersonDTO changeStatusOrderFromPerson(long userId, String orderId, OrderStatus orderStatus);
}
