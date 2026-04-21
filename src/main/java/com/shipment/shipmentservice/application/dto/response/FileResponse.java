package com.shipment.shipmentservice.application.dto.response;

public record FileResponse(
        byte[] content,
        String fileName,
        String contentType
) {}