package com.hackaton.toncash.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Deal {
    @Id
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
