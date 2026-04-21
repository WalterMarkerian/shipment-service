package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.UpdateShipmentUseCase;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateShipmentService implements UpdateShipmentUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    public UpdateShipmentService(ShipmentRepository repository, ShipmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ShipmentResponse execute(UUID id, UpdateShipmentRequest request) {
        Shipment shipment = repository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("No se encontró el envío con ID: " + id));

        shipment.updateDetails(
                request.recipientName(),
                request.destinationAddress(),
                request.destinationCity(),
                request.destinationProvince(),
                request.postalCode()
        );

        Shipment updatedShipment = repository.save(shipment);

        return mapper.toResponse(updatedShipment);
    }
}