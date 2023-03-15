package com.hackaton.toncash.dto;

import com.hackaton.toncash.model.OrderLimit;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.OrderType;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class OrderDTO {
    private String id;
    private Long ownerId;
    private LocalDateTime localDateTime;
    private float amount;
    private Point location;
    private float price;
    private String currency;
    private OrderLimit limits;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private List<String> deals;

}
