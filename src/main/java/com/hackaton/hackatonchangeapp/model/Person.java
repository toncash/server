package com.hackaton.hackatonchangeapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Set;

@RedisHash
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    private String id;

    private String username;

    private Set<Order> orders;

    private int successOrdersAmount;
    private int notSuccessOrdersAmount;

    public Person(String username, Set<Order> orders) {
        this.username = username;
        this.orders = orders;
    }
}
