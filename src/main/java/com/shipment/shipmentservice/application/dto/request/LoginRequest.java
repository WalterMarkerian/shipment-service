package com.shipment.shipmentservice.application.dto.request;

public record LoginRequest(
        String username,
        String password
) {}