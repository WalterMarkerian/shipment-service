package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.UpdateShipmentStatusUseCase;
import com.shipment.shipmentservice.domain.exception.InvalidShipmentStateException;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateShipmentStatusService implements UpdateShipmentStatusUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    public UpdateShipmentStatusService(ShipmentRepository repository, ShipmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ShipmentResponse execute(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = repository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("No se encontró el envío con id: " + shipmentId));

        switch (newStatus) {
            case IN_TRANSIT -> shipment.markAsInTransit();
            case DELIVERED  -> shipment.markAsDelivered();
            case CANCELLED  -> shipment.cancel();
            default -> throw new InvalidShipmentStateException("Transición no permitida");
        }

        return mapper.toResponse(repository.save(shipment));
    }
}
