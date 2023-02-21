package com.hackaton.toncash.dto;

import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.OrderType;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.Currency;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class OrderDTO {
    private String id;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime localDateTime;
    private float amount;
    private Point location;
    private float price;
    private String currency;
    private OrderType orderType;
    private OrderStatus orderStatus;

}
