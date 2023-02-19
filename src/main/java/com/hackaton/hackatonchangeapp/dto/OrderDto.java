package com.hackaton.hackatonchangeapp.dto;

import com.hackaton.hackatonchangeapp.model.Order;
import com.hackaton.hackatonchangeapp.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class OrderDto {

    private float amount;

    private float price;
    float[] location;

    private OrderType type;

}
