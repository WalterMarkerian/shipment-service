package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;

public interface CreateShipmentUseCase {

    ShipmentResponse execute(CreateShipmentRequest request);
}