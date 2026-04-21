package com.shipment.shipmentservice.application.dto.response;

import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        String trackingCode,
        ShipmentStatus status,
        String recipientName,
        String destinationAddress,
        String destinationCity,
        String destinationProvince,
        String postalCode,
        ShipmentType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}