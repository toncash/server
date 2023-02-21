package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.exception.OrderNotFoundException;
import com.hackaton.toncash.exception.UserExistException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.Order;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.Person;
import com.hackaton.toncash.repo.PersonRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepo personRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addPerson(PersonDTO personDto) {
        if (personRepository.findById(personDto.getId()).isPresent()) {
            throw new UserExistException(personDto.getId());
        }
        personRepository.save(modelMapper.map(personDto, Person.class));
    }

    @Override
    public PersonDTO getPerson(long id, String username) {
        Person person = personRepository.findById(id).orElse(null);
        if (person == null) {
            person = new Person();
            person.setId(id);
            person.setUsername(username);
        } else if (!person.getUsername().equals(username)) {
            person.setUsername(username);
        }
        return modelMapper.map(personRepository.save(person), PersonDTO.class);
    }

    @Override
    public PersonDTO getPerson(long id) {
        Person person = personRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        PersonDTO personDto = modelMapper.map(person, PersonDTO.class);

        return modelMapper.map(person, PersonDTO.class);
    }

    @Override
    public Iterable<PersonDTO> getPersons() {
        return StreamSupport.stream(personRepository.findAll().spliterator(), false)
                .map(p -> modelMapper.map(p, PersonDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePerson(long id) {
        Person person = personRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        personRepository.delete(person);
    }

    @Override
    public PersonDTO changePerson(long id, PersonDTO personDTO) {
        Person person = personRepository.findById(personDTO.getId()).orElseThrow(() -> new UserExistException(id));
        BeanUtils.copyProperties(personDTO, person, CommonMethods.getNullPropertyNames(personDTO));

        return modelMapper.map(personRepository.save(person), PersonDTO.class);

    }

    @Override
    public PersonDTO addOrderToPerson(long userId, String orderId) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        System.out.println("person" + person.getId());
        System.out.println(person.getCurrentOrders().size());
        person.getCurrentOrders().add(orderId);
        return modelMapper.map(personRepository.save(person), PersonDTO.class);
    }

    @Override
    public PersonDTO removeOrderFromPerson(long userId, String orderId) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        person.getCurrentOrders().remove(orderId);
        return modelMapper.map(personRepository.save(person), PersonDTO.class);

    }

    @Override
    public PersonDTO changeStatusOrderFromPerson(long userId, String orderId, OrderStatus orderStatus) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (orderStatus.equals(OrderStatus.FINISH)) {
            person.getCurrentOrders().remove(orderId);
            person.getFinishedOrders().add(orderId);
        }
        if (orderStatus.equals(OrderStatus.BAD)) {
            person.getCurrentOrders().remove(orderId);
            person.getBadOrders().add(orderId);
        }
        return modelMapper.map(personRepository.save(person), PersonDTO.class);

    }
}
