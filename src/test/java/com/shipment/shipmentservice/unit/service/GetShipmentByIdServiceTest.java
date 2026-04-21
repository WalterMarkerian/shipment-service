package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.GetShipmentByIdService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetShipmentByIdService")
class GetShipmentByIdServiceTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private GetShipmentByIdService service;

    @Test
    @DisplayName("execute: ID existente → retorna ShipmentResponse")
    void execute_whenShipmentExists_shouldReturnResponse() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Shipment shipment = Shipment.restore(
                id, "ENV-20260421-00001", now, ShipmentStatus.PENDING,
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, true
        );
        ShipmentResponse expected = new ShipmentResponse(
                id, "ENV-20260421-00001", ShipmentStatus.PENDING,
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, now
        );

        when(repository.findById(id)).thenReturn(Optional.of(shipment));
        when(mapper.toResponse(shipment)).thenReturn(expected);

        ShipmentResponse result = service.execute(id);

        assertThat(result).isEqualTo(expected);
        verify(repository).findById(id);
        verify(mapper).toResponse(shipment);
    }

    @Test
    @DisplayName("execute: ID inexistente → ShipmentNotFoundException")
    void execute_whenShipmentNotFound_shouldThrowShipmentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(mapper, never()).toResponse(any());
    }
}
