package com.hackaton.toncash.dto;

import com.hackaton.toncash.model.*;
import lombok.*;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class DealDTO {
    String id;
    String addressContract;
    String addressBuyer;
    boolean contractDeployed;
    String orderId;
    String buyerId;
    String sellerId;
    int amount;
    DealStatus dealStatus;
    private LocalDateTime localDateTime;

}
