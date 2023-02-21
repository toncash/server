package com.hackaton.toncash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PersonDTO {
    private long id;
    private String username;
    private Set<String> currentOrders;
    private Set<String> finishedOrders;
    private Set<String> badOrders;
    private Set<String> community;
    private float rank;

}
