package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.CancelShipmentUseCase;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelShipmentService implements CancelShipmentUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    public CancelShipmentService(ShipmentRepository repository, ShipmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ShipmentResponse execute(UUID shipmentId) {
        Shipment shipment = repository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("No se encontró el envío con ID: " + shipmentId));

        shipment.cancel();

        return mapper.toResponse(repository.save(shipment));
    }
}