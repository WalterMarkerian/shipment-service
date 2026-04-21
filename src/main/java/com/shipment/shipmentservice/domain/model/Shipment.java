package com.shipment.shipmentservice.domain.model;

import com.shipment.shipmentservice.domain.exception.InvalidShipmentStateException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Shipment {

    private final UUID id;
    private final String trackingCode;
    private final LocalDateTime createdAt;

    private ShipmentStatus status;
    private String recipientName;
    private String destinationAddress;
    private String destinationCity;
    private String destinationProvince;
    private String postalCode;
    private ShipmentType type;
    private LocalDateTime updatedAt;
    private boolean active;

    private Shipment(UUID id, String trackingCode, LocalDateTime createdAt, ShipmentStatus status,
                     String recipientName, String destinationAddress, String destinationCity,
                     String destinationProvince, String postalCode, ShipmentType type,
                     LocalDateTime updatedAt, boolean active) {
        this.id = id;
        this.trackingCode = trackingCode;
        this.createdAt = createdAt;
        this.status = status;
        this.recipientName = recipientName;
        this.destinationAddress = destinationAddress;
        this.destinationCity = destinationCity;
        this.destinationProvince = destinationProvince;
        this.postalCode = postalCode;
        this.type = type;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    public static Shipment create(String trackingCode, String recipientName, String address,
                                  String city, String province, String postalCode, ShipmentType type) {

        validateField(recipientName, "nombre del destinatario");
        validateField(address, "dirección de destino");
        validateField(city, "ciudad de destino");
        validateField(province, "provincia de destino");
        validateField(postalCode, "código postal");
        if (type == null) throw new IllegalArgumentException("El tipo de envío es obligatorio");

        LocalDateTime now = LocalDateTime.now();
        return new Shipment(UUID.randomUUID(), trackingCode, now, ShipmentStatus.PENDING,
                recipientName, address, city, province, postalCode, type, now, true);
    }

    private static void validateField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El campo " + fieldName + " no debe ser nulo ni vacío");
        }
    }

    public static Shipment restore(UUID id, String trackingCode, LocalDateTime createdAt,
                                   ShipmentStatus status, String recipientName, String address,
                                   String city, String province, String postalCode, ShipmentType type,
                                   LocalDateTime updatedAt, boolean active) {
        return new Shipment(id, trackingCode, createdAt, status, recipientName, address,
                city, province, postalCode, type, updatedAt, active);
    }

    public void updateDetails(String recipientName,
                              String address,
                              String city,
                              String province,
                              String postalCode) {

        if (this.status == ShipmentStatus.DELIVERED || this.status == ShipmentStatus.CANCELLED) {
            throw new InvalidShipmentStateException(
                    "No se puede modificar un envío en estado " + this.status
            );
        }

        if (!this.active) {
            throw new InvalidShipmentStateException("No se puede modificar un envío cancelado/inactivo");
        }

        this.recipientName = recipientName;
        this.destinationAddress = address;
        this.destinationCity = city;
        this.destinationProvince = province;
        this.postalCode = postalCode;

        touch();
    }


    public void markAsInTransit() {
        if (this.status != ShipmentStatus.PENDING) {
            throw new InvalidShipmentStateException("Transición inválida. Solo PENDING → EN_TRANSITO");
        }
        this.status = ShipmentStatus.IN_TRANSIT;
        touch();
    }

    public void markAsDelivered() {
        if (this.status != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidShipmentStateException("Transición inválida. Solo EN_TRANSITO → ENTREGADO");
        }
        this.status = ShipmentStatus.DELIVERED;
        touch();
    }

    public void cancel() {
        if (this.status != ShipmentStatus.PENDING) {
            throw new InvalidShipmentStateException("No se puede cancelar el envío porque su estado actual es " + this.status +
                    ". Solo los envíos PENDIENTES pueden cancelarse.");
        }
        this.status = ShipmentStatus.CANCELLED;
        this.active = false;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}