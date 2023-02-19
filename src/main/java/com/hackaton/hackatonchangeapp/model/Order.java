package com.hackaton.hackatonchangeapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;


@RedisHash
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;


    private LocalDateTime localDateTime;
    private float amount;

    private float[] location;

    private float price;

    private OrderType type;

    public Order(LocalDateTime localDateTime, float amount, float price, OrderType type, float[] location) {
        this.localDateTime = localDateTime;
        this.amount = amount;
        this.price = price;
        this.type = type;
        this.location = location;
    }
}
