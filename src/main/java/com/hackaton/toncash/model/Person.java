package com.hackaton.toncash.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
@Document(collection = "persons")
public class Person {
    @Id
    private long id;

    private long telegramId;

    private String username;

    private String avatarURL;
    private Set<String> currentOrders;
    private Set<String> finishedOrders;
    private Set<String> badOrders;

    private List<Deal> currentDeals;
    private Set<String> community;

    private PersonLevel level;
    private float rank;

    public Person() {
        this.currentOrders = new HashSet<>();
        this.finishedOrders = new HashSet<>();
        this.badOrders = new HashSet<>();
        this.currentDeals = new ArrayList<>();
        this.community = new HashSet<>();
        this.level = PersonLevel.BASIC;
    }
}
