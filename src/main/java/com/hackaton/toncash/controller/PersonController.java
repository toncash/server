package com.hackaton.toncash.controller;

import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/persons")
@CrossOrigin
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;

    @GetMapping("{id}")
    public PersonDTO getPerson(@PathVariable long id, @RequestParam(defaultValue = "") String username) {
        if (!username.isEmpty()) {
            System.out.println(id + username);
            return personService.getPerson(id, username);
        } else {
            System.out.println(id);
            return personService.getPerson(id);
        }
    }
}

