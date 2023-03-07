package com.hackaton.toncash.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@NonNull
public class PersonDTO {
    private long id;
    private long chatId;
    private String username;
    private Set<String> currentOrders;
    private int finishedOrders;
    private int badOrders;
    private Set<String> community;
    private float rank;

}
