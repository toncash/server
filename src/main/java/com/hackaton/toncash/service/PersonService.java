package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.model.OrderStatus;

public interface PersonService {
    void addPerson(PersonDTO personDto);

    PersonDTO getPerson(long id, String username);
    PersonDTO getPerson(long id);

    Iterable<PersonDTO> getPersons();

    void deletePerson(long id);

    PersonDTO changePerson(long id, PersonDTO personDto);

    PersonDTO addOrderToPerson(long userId, String orderId);
    PersonDTO removeOrderFromPerson(long userId, String orderId);
    PersonDTO changeStatusOrderFromPerson(long userId, String orderId, OrderStatus orderStatus);


}
