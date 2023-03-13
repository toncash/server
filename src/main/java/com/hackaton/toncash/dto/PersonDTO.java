package com.hackaton.toncash.dto;

import com.hackaton.toncash.model.Deal;
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
    private long chatId;
    private String username;
    private String avatarURL;
    private Set<String> currentOrders;
    private List<Deal> currentDeals;
    private int finishedOrders;
    private int badOrders;
    private Set<String> community;
    private float rank;

}
