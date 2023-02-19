package com.hackaton.hackatonchangeapp.service;

import com.hackaton.hackatonchangeapp.dto.OrderDto;
import com.hackaton.hackatonchangeapp.dto.PersonDto;
import com.hackaton.hackatonchangeapp.model.Person;

import java.util.ArrayList;

public interface AppService {
    void addPerson(String username);

    PersonDto getPerson(String username);

    ArrayList<PersonDto> getPersons();

    void deletePerson(String username);

    Person changePerson(String username);

    PersonDto addOrderToPerson(String username, OrderDto orderDto);


}
