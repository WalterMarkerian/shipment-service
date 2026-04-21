package com.shipment.shipmentservice.application.dto.request;

import com.shipment.shipmentservice.domain.model.ShipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShipmentRequest(
        @NotBlank(message = "El nombre del destinatario es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String recipientName,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String destinationAddress,

        @NotBlank(message = "La ciudad es obligatoria")
        String destinationCity,

        @NotBlank(message = "La provincia es obligatoria")
        String destinationProvince,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "^\\d{4}$", message = "El código postal debe ser un formato numérico de 4 dígitos")
        String postalCode,

        @NotNull(message = "El tipo de envío es obligatorio")
        ShipmentType type
) {}