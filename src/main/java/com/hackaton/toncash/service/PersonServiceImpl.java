package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.exception.UserExistException;
import com.hackaton.toncash.exception.UserNotFoundException;
import com.hackaton.toncash.model.Deal;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.Person;
import com.hackaton.toncash.repo.DealRepository;
import com.hackaton.toncash.repo.PersonRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.hackaton.toncash.service.CommonMethods.mapToPersonDTO;

@Service
@AllArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final DealRepository dealRepository;
    private final ModelMapper modelMapper;


    @Override
    public void addPerson(PersonDTO personDto) {
        if (personRepository.findById(personDto.getId()).isPresent()) {
            throw new UserExistException(personDto.getId());
        }
        personRepository.save(modelMapper.map(personDto, Person.class));
    }

    private long createIdFromLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.toInstant(ZoneOffset.UTC);
        return instant.toEpochMilli() - ZoneOffset.UTC.getTotalSeconds() * 1000L;
    }

    @Override
    public PersonDTO entrance(PersonDTO personDTO) {
        System.out.println(personDTO.getAvatarURL());
        Person person = personRepository.findByTelegramId(personDTO.getTelegramId()).orElse(null);
        if (person == null) {
            person = new Person();
            person.setId(createIdFromLocalDateTime());
            person.setUsername(personDTO.getUsername());
            person.setTelegramId(personDTO.getTelegramId());
            person.setAvatarURL(personDTO.getAvatarURL());
        } else {
            if (person.getUsername() == null || !person.getUsername().equals(personDTO.getUsername())) {
                person.setUsername(personDTO.getUsername());
            }
            if (person.getAvatarURL() == null || !person.getAvatarURL().equals(personDTO.getAvatarURL())) {
                person.setAvatarURL(personDTO.getAvatarURL());
            }
        }
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(personRepository.save(person), deals, modelMapper);
    }

    @Override
    public PersonDTO firstInPerson(long id, PersonDTO personDTO) {
        System.out.println(personDTO.getAvatarURL());
        Person person = personRepository.findById(id).orElse(null);
        if (person == null) {
            person = new Person();
            person.setId(createIdFromLocalDateTime());
            person.setUsername(personDTO.getUsername());
            person.setTelegramId(personDTO.getTelegramId());
            person.setAvatarURL(personDTO.getAvatarURL());
        } else {
            if (person.getUsername() == null || !person.getUsername().equals(personDTO.getUsername())) {
                person.setUsername(personDTO.getUsername());
            }
            if (person.getTelegramId() != personDTO.getTelegramId()) {
                person.setTelegramId(personDTO.getTelegramId());
            }
            if (person.getAvatarURL() == null || !person.getAvatarURL().equals(personDTO.getAvatarURL())) {
                person.setAvatarURL(personDTO.getAvatarURL());
            }
        }
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(personRepository.save(person), deals, modelMapper);
    }

    @Override
    public PersonDTO getPerson(long id) {
        Person person = personRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(person, deals, modelMapper);

    }

    @Override
    public Iterable<PersonDTO> getPersons() {
        return StreamSupport.stream(personRepository.findAll().spliterator(), false)
                .map(person->CommonMethods.mapToPersonDTO(person, dealRepository.findAllById(person.getCurrentDeals()), modelMapper))
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

        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(person, deals, modelMapper);

    }

    @Override
    public PersonDTO addOrderToPerson(long personId, String orderId) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new UserNotFoundException(personId));
        person.getCurrentOrders().add(orderId);
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(person, deals, modelMapper);
    }

    @Override
    public PersonDTO removeOrderFromPerson(long personId, String orderId) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new UserNotFoundException(personId));
        person.getCurrentOrders().remove(orderId);
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(person, deals, modelMapper);

    }


    @Override
    public PersonDTO changeStatusOrderFromPerson(long personId, String orderId, OrderStatus orderStatus) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new UserNotFoundException(personId));
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
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return mapToPersonDTO(personRepository.save(person), deals, modelMapper);

    }

    private float getRank(Person person) {
        int countBadOrders = person.getBadOrders().size() != 0 ? person.getBadOrders().size() : 1;
        return (person.getFinishedOrders().size() + person.getBadOrders().size()) * 1.0f / countBadOrders;

    }

    @Override
    public Iterable<DealDTO> getDealsByPersonId(Long personId) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new UserNotFoundException(personId));
        Iterable<Deal> deals = dealRepository.findAllById(person.getCurrentDeals());
        return CommonMethods.getDealsDto(modelMapper, deals);
    }


}
