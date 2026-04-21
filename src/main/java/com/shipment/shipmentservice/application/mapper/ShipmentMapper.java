package com.shipment.shipmentservice.application.mapper;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.domain.model.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    ShipmentResponse toResponse(Shipment shipment);
}