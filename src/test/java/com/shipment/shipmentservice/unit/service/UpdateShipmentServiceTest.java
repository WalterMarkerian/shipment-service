package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.UpdateShipmentService;
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
@DisplayName("UpdateShipmentService")
class UpdateShipmentServiceTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private UpdateShipmentService service;

    private static final UpdateShipmentRequest VALID_REQUEST = new UpdateShipmentRequest(
            "Nuevo Nombre", "Nueva Dirección", "Nueva Ciudad", "Nueva Provincia", "9999"
    );

    @Test
    @DisplayName("execute: envío PENDING → actualiza y retorna respuesta")
    void execute_pendingShipment_shouldUpdateAndReturnResponse() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.PENDING);
        ShipmentResponse response = buildResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any(Shipment.class))).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id, VALID_REQUEST);

        assertThat(result).isNotNull();
        assertThat(shipment.getRecipientName()).isEqualTo("Nuevo Nombre");
        assertThat(shipment.getDestinationCity()).isEqualTo("Nueva Ciudad");
        verify(repository).save(shipment);
    }

    @Test
    @DisplayName("execute: envío IN_TRANSIT → actualiza exitosamente")
    void execute_inTransitShipment_shouldUpdateSuccessfully() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.IN_TRANSIT);
        ShipmentResponse response = buildResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(repository.save(any(Shipment.class))).thenReturn(shipment);
        when(mapper.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = service.execute(id, VALID_REQUEST);

        assertThat(result).isNotNull();
        verify(repository).save(shipment);
    }

    @Test
    @DisplayName("execute: envío DELIVERED → InvalidShipmentStateException")
    void execute_deliveredShipment_shouldThrowInvalidShipmentStateException() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.DELIVERED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id, VALID_REQUEST))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: envío CANCELLED → InvalidShipmentStateException")
    void execute_cancelledShipment_shouldThrowInvalidShipmentStateException() {
        UUID id = UUID.randomUUID();
        Shipment shipment = buildShipment(id, ShipmentStatus.CANCELLED);

        when(repository.findById(id)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.execute(id, VALID_REQUEST))
                .isInstanceOf(InvalidShipmentStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute: ID inexistente → ShipmentNotFoundException")
    void execute_whenShipmentNotFound_shouldThrowShipmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id, VALID_REQUEST))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining(id.toString());

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

    private ShipmentResponse buildResponse(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return new ShipmentResponse(
                id, "ENV-20260421-00001", ShipmentStatus.PENDING,
                "Nuevo Nombre", "Nueva Dirección", "Nueva Ciudad",
                "Nueva Provincia", "9999", ShipmentType.ESTANDAR, now, now
        );
    }
}
