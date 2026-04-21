package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.response.FileResponse;

public interface GenerateShipmentReportUseCase {
    FileResponse execute();
}