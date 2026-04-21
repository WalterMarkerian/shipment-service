package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import java.util.UUID;

public interface UpdateShipmentUseCase {
    ShipmentResponse execute(UUID id, UpdateShipmentRequest request);
}