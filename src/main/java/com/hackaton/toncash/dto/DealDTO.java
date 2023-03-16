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
    Boolean contractDeployed;
    String orderId;
    Long buyerId;
    Long sellerId;
    Integer amount;
    DealStatus dealStatus;
    private LocalDateTime localDateTime;

}
