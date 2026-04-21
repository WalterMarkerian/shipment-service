package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.GetShipmentByTrackingUseCase;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetShipmentByTrackingService implements GetShipmentByTrackingUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse execute(String trackingCode) {
        return repository.findByTrackingCode(trackingCode)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ShipmentNotFoundException("No se encontró el envío con código: " + trackingCode));
    }
}