package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.application.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.service.CreateShipmentService;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import com.shipment.shipmentservice.domain.service.TrackingCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateShipmentService")
class CreateShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private TrackingCodeGenerator trackingCodeGenerator;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private CreateShipmentService service;

    private CreateShipmentRequest validRequest;
    private Shipment savedShipment;
    private ShipmentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new CreateShipmentRequest(
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR
        );

        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        savedShipment = Shipment.restore(
                id, "ENV-20260421-00001", now, ShipmentStatus.PENDING,
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, true
        );

        expectedResponse = new ShipmentResponse(
                id, "ENV-20260421-00001", ShipmentStatus.PENDING,
                "Juan Pérez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, now
        );
    }

    @Test
    @DisplayName("execute: petición válida → crea y retorna el envío")
    void execute_withValidRequest_shouldCreateAndReturnShipment() {
        when(trackingCodeGenerator.generate()).thenReturn("ENV-20260421-00001");
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);
        when(shipmentMapper.toResponse(savedShipment)).thenReturn(expectedResponse);

        ShipmentResponse result = service.execute(validRequest);

        assertThat(result).isEqualTo(expectedResponse);
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(trackingCodeGenerator, times(1)).generate();
    }

    @Test
    @DisplayName("execute: primer código duplicado → reintenta y tiene éxito")
    void execute_whenDuplicateTrackingCodeOnFirstAttempt_shouldRetryAndSucceed() {
        DataIntegrityViolationException duplicateEx = buildDuplicateTrackingException();

        when(trackingCodeGenerator.generate())
                .thenReturn("ENV-20260421-00001")
                .thenReturn("ENV-20260421-00002");
        when(shipmentRepository.save(any(Shipment.class)))
                .thenThrow(duplicateEx)
                .thenReturn(savedShipment);
        when(shipmentMapper.toResponse(savedShipment)).thenReturn(expectedResponse);

        ShipmentResponse result = service.execute(validRequest);

        assertThat(result).isEqualTo(expectedResponse);
        verify(shipmentRepository, times(2)).save(any(Shipment.class));
    }

    @Test
    @DisplayName("execute: siempre código duplicado → RuntimeException tras 3 intentos")
    void execute_whenAlwaysDuplicateTrackingCode_shouldThrowAfterMaxRetries() {
        DataIntegrityViolationException duplicateEx = buildDuplicateTrackingException();

        when(trackingCodeGenerator.generate()).thenReturn("ENV-20260421-00001");
        when(shipmentRepository.save(any(Shipment.class))).thenThrow(duplicateEx);

        assertThatThrownBy(() -> service.execute(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("trackingCode");

        verify(shipmentRepository, times(3)).save(any(Shipment.class));
    }

    @Test
    @DisplayName("execute: excepción de integridad no relacionada a tracking_code → propaga la excepción")
    void execute_whenNonDuplicateDataIntegrityViolation_shouldPropagateException() {
        DataIntegrityViolationException otherEx =
                new DataIntegrityViolationException("otra restricción violada");

        when(trackingCodeGenerator.generate()).thenReturn("ENV-20260421-00001");
        when(shipmentRepository.save(any(Shipment.class))).thenThrow(otherEx);

        assertThatThrownBy(() -> service.execute(validRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    private DataIntegrityViolationException buildDuplicateTrackingException() {
        RuntimeException cause = new RuntimeException(
                "ERROR: duplicate key value violates unique constraint. Key (tracking_code)=(ENV-20260421-00001) already exists."
        );
        return new DataIntegrityViolationException("constraint violation", cause);
    }
}
