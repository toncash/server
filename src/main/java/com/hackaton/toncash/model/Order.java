package com.hackaton.toncash.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private Long ownerId;
    private Integer amount;
    @GeoSpatialIndexed(name = "location", type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Point location;
    private Float price;
    private String currency;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private OrderLimit limits;
    List<Deal> deals;
    private LocalDateTime localDateTime;

}
