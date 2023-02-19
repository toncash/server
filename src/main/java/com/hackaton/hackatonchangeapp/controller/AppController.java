package com.hackaton.hackatonchangeapp.controller;

import com.hackaton.hackatonchangeapp.dto.Data;
import com.hackaton.hackatonchangeapp.service.AppService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@RequestMapping("api/v1")
@CrossOrigin
@AllArgsConstructor
public class AppController {
    private final AppService appService;


    @PostMapping("")
    public void addPerson(@PathVariable String username) {
        appService.addPerson(username);
    }


}
