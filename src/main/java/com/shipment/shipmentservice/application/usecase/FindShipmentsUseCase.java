package com.shipment.shipmentservice.application.usecase;

import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindShipmentsUseCase {
    Page<ShipmentResponse> execute(ShipmentFilters filters, Pageable pageable);
}