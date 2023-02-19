package com.hackaton.hackatonchangeapp.dto;

import com.hackaton.hackatonchangeapp.model.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class PersonDto {

    private String username;

    private Set<Order> orders;

}
