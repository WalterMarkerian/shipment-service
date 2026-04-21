package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import java.util.UUID;

public interface GetShipmentByIdUseCase {
    ShipmentResponse execute(UUID id);
}