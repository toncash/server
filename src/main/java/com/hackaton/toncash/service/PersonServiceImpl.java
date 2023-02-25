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
import java.util.Set;
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
        return mapToPersonDTO(personRepository.save(person));
    }

    @Override
    public PersonDTO getPerson(long id) {
        return mapToPersonDTO(personRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public Iterable<PersonDTO> getPersons() {
        return StreamSupport.stream(personRepository.findAll().spliterator(), false)
                .map(this::mapToPersonDTO)
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

        return mapToPersonDTO(personRepository.save(person));

    }

    @Override
    public PersonDTO addOrderToPerson(long userId, String orderId) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        person.getCurrentOrders().add(orderId);
        return mapToPersonDTO(personRepository.save(person));
    }

    @Override
    public PersonDTO removeOrderFromPerson(long userId, String orderId) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        person.getCurrentOrders().remove(orderId);
        return mapToPersonDTO(personRepository.save(person));

    }


    @Override
    public PersonDTO changeStatusOrderFromPerson(long userId, String orderId, OrderStatus orderStatus) {
        Person person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (orderStatus.equals(OrderStatus.FINISH)) {
            person.getCurrentOrders().remove(orderId);
            person.getFinishedOrders().add(orderId);
//            person.getCommunity() // FIXME
        }
        if (orderStatus.equals(OrderStatus.BAD)) {
            person.getCurrentOrders().remove(orderId);
            person.getBadOrders().add(orderId);
        }
        person.setRank(getRank(person));
        return mapToPersonDTO(personRepository.save(person));

    }

    private float getRank(Person person) {
        int countBadOrders = person.getBadOrders().size() != 0 ? person.getBadOrders().size() : 1;
        return (person.getFinishedOrders().size() + person.getBadOrders().size()) * 1.0f / countBadOrders;

    }

    private PersonDTO mapToPersonDTO(Person person) {
        return PersonDTO.builder()
                .id(person.getId())
                .username(person.getUsername())
                .currentOrders(person.getCurrentOrders())
                .finishedOrders(person.getFinishedOrders().size())
                .badOrders(person.getBadOrders().size())
                .community(person.getCommunity())
                .rank(person.getRank())
                .build();
    }
}
