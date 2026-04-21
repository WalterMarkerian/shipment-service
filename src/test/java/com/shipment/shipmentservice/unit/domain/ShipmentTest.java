package com.shipment.shipmentservice.unit.domain;

import com.shipment.shipmentservice.domain.exception.InvalidShipmentStateException;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Shipment - Modelo de Dominio")
class ShipmentTest {

    private static final String TRACKING_CODE = "ENV-20260421-00001";

    // ─────────────────────────────────────────────
    // create()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("create: datos válidos → envío en estado PENDING activo")
    void create_withValidData_shouldCreatePendingActiveShipment() {
        Shipment shipment = Shipment.create(
                TRACKING_CODE, "Juan Pérez", "Av. Santa Fe 1234",
                "CABA", "Buenos Aires", "1425", ShipmentType.ESTANDAR
        );

        assertThat(shipment.getId()).isNotNull();
        assertThat(shipment.getTrackingCode()).isEqualTo(TRACKING_CODE);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(shipment.getRecipientName()).isEqualTo("Juan Pérez");
        assertThat(shipment.getDestinationCity()).isEqualTo("CABA");
        assertThat(shipment.getType()).isEqualTo(ShipmentType.ESTANDAR);
        assertThat(shipment.isActive()).isTrue();
        assertThat(shipment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create: tipo null → IllegalArgumentException")
    void create_withNullType_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> Shipment.create(
                TRACKING_CODE, "Juan", "Dir 1", "CABA", "BA", "1000", null
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: nombre destinatario en blanco → IllegalArgumentException")
    void create_withBlankRecipientName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> Shipment.create(
                TRACKING_CODE, "  ", "Dir 1", "CABA", "BA", "1000", ShipmentType.ESTANDAR
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: dirección null → IllegalArgumentException")
    void create_withNullAddress_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> Shipment.create(
                TRACKING_CODE, "Juan", null, "CABA", "BA", "1000", ShipmentType.ESTANDAR
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ─────────────────────────────────────────────
    // markAsInTransit()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("markAsInTransit: PENDING → IN_TRANSIT")
    void markAsInTransit_fromPending_shouldTransitionToInTransit() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);

        shipment.markAsInTransit();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(shipment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsInTransit: IN_TRANSIT → excepción")
    void markAsInTransit_fromInTransit_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);

        assertThatThrownBy(shipment::markAsInTransit)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("markAsInTransit: DELIVERED → excepción")
    void markAsInTransit_fromDelivered_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);

        assertThatThrownBy(shipment::markAsInTransit)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("markAsInTransit: CANCELLED → excepción")
    void markAsInTransit_fromCancelled_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.CANCELLED);

        assertThatThrownBy(shipment::markAsInTransit)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    // ─────────────────────────────────────────────
    // markAsDelivered()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("markAsDelivered: IN_TRANSIT → DELIVERED")
    void markAsDelivered_fromInTransit_shouldTransitionToDelivered() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);

        shipment.markAsDelivered();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    @DisplayName("markAsDelivered: PENDING → excepción")
    void markAsDelivered_fromPending_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);

        assertThatThrownBy(shipment::markAsDelivered)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("markAsDelivered: DELIVERED → excepción")
    void markAsDelivered_fromDelivered_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);

        assertThatThrownBy(shipment::markAsDelivered)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    // ─────────────────────────────────────────────
    // cancel()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("cancel: PENDING → CANCELLED y active=false")
    void cancel_fromPending_shouldTransitionToCancelledAndSetInactive() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);

        shipment.cancel();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.isActive()).isFalse();
    }

    @Test
    @DisplayName("cancel: IN_TRANSIT → excepción")
    void cancel_fromInTransit_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);

        assertThatThrownBy(shipment::cancel)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("cancel: DELIVERED → excepción")
    void cancel_fromDelivered_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);

        assertThatThrownBy(shipment::cancel)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("cancel: CANCELLED → excepción")
    void cancel_alreadyCancelled_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.CANCELLED);

        assertThatThrownBy(shipment::cancel)
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    // ─────────────────────────────────────────────
    // updateDetails()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("updateDetails: PENDING → actualiza campos correctamente")
    void updateDetails_fromPending_shouldUpdateFields() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);

        shipment.updateDetails("Nuevo Nombre", "Nueva Dir", "Nueva Ciudad", "Nueva Provincia", "9999");

        assertThat(shipment.getRecipientName()).isEqualTo("Nuevo Nombre");
        assertThat(shipment.getDestinationAddress()).isEqualTo("Nueva Dir");
        assertThat(shipment.getDestinationCity()).isEqualTo("Nueva Ciudad");
        assertThat(shipment.getDestinationProvince()).isEqualTo("Nueva Provincia");
        assertThat(shipment.getPostalCode()).isEqualTo("9999");
    }

    @Test
    @DisplayName("updateDetails: IN_TRANSIT → actualiza campos correctamente")
    void updateDetails_fromInTransit_shouldUpdateFields() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);

        shipment.updateDetails("Otro Nombre", "Otra Dir", "Otra Ciudad", "Otra Prov", "1234");

        assertThat(shipment.getRecipientName()).isEqualTo("Otro Nombre");
    }

    @Test
    @DisplayName("updateDetails: DELIVERED → excepción")
    void updateDetails_fromDelivered_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);

        assertThatThrownBy(() -> shipment.updateDetails("A", "B", "C", "D", "1234"))
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    @Test
    @DisplayName("updateDetails: CANCELLED (inactive) → excepción")
    void updateDetails_fromCancelled_shouldThrowInvalidShipmentStateException() {
        Shipment shipment = buildShipment(ShipmentStatus.CANCELLED);

        assertThatThrownBy(() -> shipment.updateDetails("A", "B", "C", "D", "1234"))
                .isInstanceOf(InvalidShipmentStateException.class);
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private Shipment buildShipment(ShipmentStatus status) {
        boolean active = status != ShipmentStatus.CANCELLED;
        return Shipment.restore(
                UUID.randomUUID(), TRACKING_CODE, LocalDateTime.now(),
                status, "Juan Pérez", "Av. Santa Fe 1234",
                "CABA", "Buenos Aires", "1425",
                ShipmentType.ESTANDAR, LocalDateTime.now(), active
        );
    }
}
