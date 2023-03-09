package com.hackaton.toncash.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
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

    private long chatId;

    private String username;

    private String avatarURL;
    private Set<String> currentOrders;
    private Set<String> finishedOrders;
    private Set<String> badOrders;

    private Set<String> community;

    private PersonLevel level;
    private float rank;

    public Person() {
        this.currentOrders = new HashSet<>();
        this.finishedOrders = new HashSet<>();
        this.badOrders = new HashSet<>();
        this.community = new HashSet<>();
        this.level = PersonLevel.BASIC;
    }
}
