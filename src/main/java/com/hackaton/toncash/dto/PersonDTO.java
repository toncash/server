package com.hackaton.toncash.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@NonNull
public class PersonDTO {
    private long id;
    private long telegramId;
    private String username;
    private String avatarURL;
    private Set<String> currentOrders;
    private Set<String> currentDeals;
    private int finishedOrders;
    private int badOrders;
    private Set<String> community;
    private float rank;

}
