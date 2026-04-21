package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import java.util.Map;

public interface ReportExporter {
    byte[] exportToPdf(Map<ShipmentStatus, Long> data);
}