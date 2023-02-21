package com.hackaton.toncash.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Currency;


@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Builder
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private Long buyerId;
    private Long sellerId;
    private float amount;
    @GeoSpatialIndexed(name = "location", type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point location;
    private float price;
    private String currency;
    private OrderType orderType;
    private OrderStatus orderStatus;

    private LocalDateTime localDateTime;

    public Order() {
        this.localDateTime = LocalDateTime.now();
    }


}
