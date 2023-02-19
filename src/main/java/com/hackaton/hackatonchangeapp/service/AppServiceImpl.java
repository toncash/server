package com.hackaton.hackatonchangeapp.service;

import com.hackaton.hackatonchangeapp.dto.OrderDto;
import com.hackaton.hackatonchangeapp.dto.PersonDto;
import com.hackaton.hackatonchangeapp.exception.UserExistException;
import com.hackaton.hackatonchangeapp.exception.UserNotFoundException;
import com.hackaton.hackatonchangeapp.model.Order;
import com.hackaton.hackatonchangeapp.model.Person;
import com.hackaton.hackatonchangeapp.repo.PersonRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class AppServiceImpl implements AppService {

    private final PersonRepo personRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addPerson(String username) {
        if (personRepository.findByUsername(username).isPresent()) {
            throw new UserExistException(username);
        }
        Person person = new Person(username, new HashSet<>());
    }

    @Override
    public PersonDto getPerson(String username) {
        Person person = personRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        return modelMapper.map(person, PersonDto.class);
    }

    @Override
    public ArrayList<PersonDto> getPersons() {
        return null;
    }

    @Override
    public void deletePerson(String username) {

    }

    @Override
    public Person changePerson(String username) {
        return null;
    }

    @Override
    public PersonDto addOrderToPerson(String username, OrderDto orderDto) {
        Person person = personRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        Order order = modelMapper.map(orderDto, Order.class);
        person.getOrders().add(order);
        return modelMapper.map(personRepository.save(person), PersonDto.class);
    }
}
