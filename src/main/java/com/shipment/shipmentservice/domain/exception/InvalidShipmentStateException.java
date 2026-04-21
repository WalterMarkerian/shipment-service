package com.shipment.shipmentservice.domain.exception;

public class InvalidShipmentStateException extends DomainException {

    public InvalidShipmentStateException(String message) {
        super(message, "INVALID_SHIPMENT_STATE");
    }
}