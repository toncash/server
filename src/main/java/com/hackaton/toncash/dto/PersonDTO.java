package com.hackaton.toncash.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PersonDTO {
    private long id;
    private String username;
    private Set<String> currentOrders;
    private int finishedOrders;
    private int badOrders;
    private Set<String> community;
    private float rank;

}
