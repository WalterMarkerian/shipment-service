package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.CancelShipmentService;
import com.shipment.shipmentservice.domain.exception.InvalidShipmentStateException;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelShipmentService")
class CancelShipmentServiceTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private CancelShipmentService service;

    @Test
    @DisplayName("execute: envío PENDING → se cancela y retorna respuesta")
    void execute_pendingShipment_shouldCancelAndReturnResponse() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.PENDING);
        ShipmentResponse response = buildResponse(id, ShipmentStatus.CANCELLED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any(Shipment.class))).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id);

        assertThat(result.status()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.isActive()).isFalse();
        verify(repository).save(shipment);
    }

    @Test
    @DisplayName("execute: ID inexistente → ShipmentNotFoundException")
    void execute_whenShipmentNotFound_shouldThrowShipmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: envío EN_TRANSITO → InvalidShipmentStateException")
    void execute_inTransitShipment_shouldThrowInvalidShipmentStateException() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.IN_TRANSIT);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: envío ENTREGADO → InvalidShipmentStateException")
    void execute_deliveredShipment_shouldThrowInvalidShipmentStateException() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.DELIVERED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    private Shipment buildShipment(UUID id, ShipmentStatus status) {
        boolean active = status != ShipmentStatus.CANCELLED;
        return Shipment.restore(
                id, "ENV-20260421-00001", LocalDateTime.now(),
                status, "Juan Pérez", "Av. Santa Fe 1234",
                "CABA", "Buenos Aires", "1425",
                ShipmentType.ESTANDAR, LocalDateTime.now(), active
        );
    }

    private ShipmentResponse buildResponse(UUID id, ShipmentStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new ShipmentResponse(
                id, "ENV-20260421-00001", status,
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, now
        );
    }
}
