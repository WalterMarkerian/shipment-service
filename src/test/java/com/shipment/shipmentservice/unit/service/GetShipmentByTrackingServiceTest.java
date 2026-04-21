package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.GetShipmentByTrackingService;
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
@DisplayName("GetShipmentByTrackingService")
class GetShipmentByTrackingServiceTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private GetShipmentByTrackingService service;

    private static final String TRACKING_CODE = "ENV-20260421-00001";

    @Test
    @DisplayName("execute: código existente → retorna ShipmentResponse")
    void execute_whenTrackingCodeExists_shouldReturnResponse() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Shipment shipment = Shipment.restore(
                id, TRACKING_CODE, now, ShipmentStatus.IN_TRANSIT,
                "María García", "Calle Falsa 123", "La Plata",
                "Buenos Aires", "1900", ShipmentType.EXPRESO, now, true
        );
        ShipmentResponse expected = new ShipmentResponse(
                id, TRACKING_CODE, ShipmentStatus.IN_TRANSIT,
                "María García", "Calle Falsa 123", "La Plata",
                "Buenos Aires", "1900", ShipmentType.EXPRESO, now, now
        );

        when(repository.findByTrackingCode(TRACKING_CODE)).thenReturn(Optional.of(shipment));
        when(mapper.toResponse(shipment)).thenReturn(expected);

        ShipmentResponse result = service.execute(TRACKING_CODE);

        assertThat(result).isEqualTo(expected);
        assertThat(result.trackingCode()).isEqualTo(TRACKING_CODE);
        verify(repository).findByTrackingCode(TRACKING_CODE);
    }

    @Test
    @DisplayName("execute: código inexistente → ShipmentNotFoundException")
    void execute_whenTrackingCodeNotFound_shouldThrowShipmentNotFoundException() {
        when(repository.findByTrackingCode(TRACKING_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(TRACKING_CODE))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining(TRACKING_CODE);

        verify(mapper, never()).toResponse(any());
    }
}
