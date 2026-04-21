package com.shipment.shipmentservice.application.dto.request;

import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;

public record ShipmentFilters(
        ShipmentStatus status,
        ShipmentType type,
        String destinationCity
) {}