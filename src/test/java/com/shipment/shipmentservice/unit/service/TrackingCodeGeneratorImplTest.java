package com.shipment.shipmentservice.unit.service;

import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import com.shipment.shipmentservice.infrastructure.service.TrackingCodeGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackingCodeGeneratorImpl")
class TrackingCodeGeneratorImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private TrackingCodeGeneratorImpl generator;

    @Test
    @DisplayName("generate: retorna código con formato ENV-YYYYMMDD-NNNNN")
    void generate_shouldReturnCorrectFormat() {
        when(shipmentRepository.getNextSequenceValue()).thenReturn(1L);

        String code = generator.generate();

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThat(code).isEqualTo("ENV-" + todayDate + "-00001");
        assertThat(code).matches("ENV-\\d{8}-\\d{5}");
    }

    @Test
    @DisplayName("generate: secuencia mayor a 1 → código formateado con ceros a la izquierda")
    void generate_withLargerSequence_shouldPadWithZeros() {
        when(shipmentRepository.getNextSequenceValue()).thenReturn(42L);

        String code = generator.generate();

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThat(code).isEqualTo("ENV-" + todayDate + "-00042");
    }

    @Test
    @DisplayName("generate: secuencia máxima (99999) → cinco dígitos sin truncar")
    void generate_withMaxSequence_shouldReturnFiveDigits() {
        when(shipmentRepository.getNextSequenceValue()).thenReturn(99999L);

        String code = generator.generate();

        assertThat(code).endsWith("-99999");
    }

    @Test
    @DisplayName("generate: cada llamada invoca el repositorio para obtener el siguiente número de secuencia")
    void generate_shouldCallRepositoryForEachCode() {
        when(shipmentRepository.getNextSequenceValue()).thenReturn(1L, 2L, 3L);

        generator.generate();
        generator.generate();
        generator.generate();

        verify(shipmentRepository, times(3)).getNextSequenceValue();
    }
}
