package com.shipment.shipmentservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateShipmentRequest(
        @NotBlank(message = "El nombre del destinatario es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String recipientName,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String destinationAddress,

        @NotBlank(message = "La ciudad de destino es obligatoria")
        String destinationCity,

        @NotBlank(message = "La provincia de destino es obligatoria")
        String destinationProvince,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "^\\d{4}$", message = "El código postal debe ser de 4 dígitos")
        String postalCode
) {}