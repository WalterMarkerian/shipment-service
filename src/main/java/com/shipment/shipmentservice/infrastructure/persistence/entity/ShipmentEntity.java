package com.shipment.shipmentservice.infrastructure.persistence.entity;

import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEntity {

    @Id
    private UUID id;

    @Column(name = "tracking_code", unique = true, nullable = false)
    private String trackingCode;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 255)
    private String destinationAddress;

    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false)
    private String destinationProvince;

    @Column(nullable = false, length = 4)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active;
}