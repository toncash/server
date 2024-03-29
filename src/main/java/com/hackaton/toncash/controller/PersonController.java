package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.DealDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.dto.PersonDealDTO;
import com.hackaton.toncash.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/persons")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;

    @PostMapping("{id}")
    public PersonDTO firstInPerson(@PathVariable long id, @RequestBody PersonDTO personDTO) {
        return personService.firstInPerson(id, personDTO);
    }

    @GetMapping("{id}")
    public PersonDTO getPerson(@PathVariable long id) {
        return personService.getPerson(id);
    }

    @GetMapping("{id}/deals")
    public Iterable<PersonDealDTO> getDealsByPersonId(@PathVariable Long id) {
        System.out.println(id);
        return personService.getDealsByPersonId(id);
    }
}

