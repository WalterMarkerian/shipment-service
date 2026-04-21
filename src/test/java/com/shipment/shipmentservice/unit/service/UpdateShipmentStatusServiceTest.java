package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.UpdateShipmentStatusService;
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
@DisplayName("UpdateShipmentStatusService")
class UpdateShipmentStatusServiceTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private UpdateShipmentStatusService service;

    @Test
    @DisplayName("execute: PENDING → IN_TRANSIT correctamente")
    void execute_pendingToInTransit_shouldTransitionAndSave() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.PENDING);
        ShipmentResponse response = buildResponse(id, ShipmentStatus.IN_TRANSIT);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any())).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id, ShipmentStatus.IN_TRANSIT);

        assertThat(result.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        verify(repository).save(shipment);
    }

    @Test
    @DisplayName("execute: IN_TRANSIT → DELIVERED correctamente")
    void execute_inTransitToDelivered_shouldTransitionAndSave() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.IN_TRANSIT);
        ShipmentResponse response = buildResponse(id, ShipmentStatus.DELIVERED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any())).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id, ShipmentStatus.DELIVERED);

        assertThat(result.status()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    @DisplayName("execute: PENDING → CANCELLED correctamente (vía cancel)")
    void execute_pendingToCancelled_shouldCancelAndSave() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.PENDING);
        ShipmentResponse response = buildResponse(id, ShipmentStatus.CANCELLED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any())).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id, ShipmentStatus.CANCELLED);

        assertThat(result.status()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.isActive()).isFalse();
    }

    @Test
    @DisplayName("execute: PENDING → PENDING inválido → InvalidShipmentStateException")
    void execute_pendingToPending_shouldThrowInvalidShipmentStateException() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.PENDING);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        // PENDING is not handled by the switch → InvalidShipmentStateException default
        assertThatThrownBy(() -> service.execute(id, ShipmentStatus.PENDING))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: IN_TRANSIT → IN_TRANSIT → InvalidShipmentStateException desde dominio")
    void execute_inTransitToInTransit_shouldThrowFromDomain() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.IN_TRANSIT);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id, ShipmentStatus.IN_TRANSIT))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: DELIVERED → CANCELLED → InvalidShipmentStateException desde dominio")
    void execute_deliveredToCancelled_shouldThrowFromDomain() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.DELIVERED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id, ShipmentStatus.CANCELLED))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: ID inexistente → ShipmentNotFoundException")
    void execute_whenShipmentNotFound_shouldThrowShipmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id, ShipmentStatus.IN_TRANSIT))
                .isInstanceOf(ShipmentNotFoundException.class);

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
