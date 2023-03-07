package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/persons")
@CrossOrigin(origins = "/**", allowedHeaders = "/**")
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;

    @PostMapping("{id}")
    public PersonDTO firstInPerson(@PathVariable long id, @RequestBody PersonDTO personDTO) {
        System.out.println(personDTO.getChatId());
        System.out.println(personDTO.getUsername());
        return personService.firstInPerson(id, personDTO);
    }

    @GetMapping("{id}")
    public PersonDTO getPerson(@PathVariable long id) {
        return personService.getPerson(id);
    }
}

