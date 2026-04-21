package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;

import java.util.UUID;

public interface UpdateShipmentStatusUseCase {
    ShipmentResponse execute(UUID shipmentId, ShipmentStatus newStatus);
}