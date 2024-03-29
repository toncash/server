package com.hackaton.toncash.dto;

import com.hackaton.toncash.model.Deal;
import com.hackaton.toncash.model.OrderLimit;
import com.hackaton.toncash.model.OrderStatus;
import com.hackaton.toncash.model.OrderType;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@NonNull
public class OrderDTO {
    private String id;
    private Long ownerId;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime localDateTime;
    private Integer amount;
    private Point location;
    private Float price;
    private String currency;
    private OrderLimit limits;
    private OrderType orderType;
    private OrderStatus orderStatus;
    List<Deal> deals;

}
