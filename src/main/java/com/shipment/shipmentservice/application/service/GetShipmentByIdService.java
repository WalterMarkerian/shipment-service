package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.GetShipmentByIdUseCase;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetShipmentByIdService implements GetShipmentByIdUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse execute(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ShipmentNotFoundException("No se encontró el envío con ID: " + id));
    }
}