package com.shipment.shipmentservice.infrastructure.service;

import com.shipment.shipmentservice.domain.service.TrackingCodeGenerator;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class TrackingCodeGeneratorImpl implements TrackingCodeGenerator {

    private final ShipmentRepository shipmentRepository;
    private static final String PREFIX = "ENV";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);

        Long nextSequence = shipmentRepository.getNextSequenceValue();

        String sequencePart = String.format("%05d", nextSequence);

        return String.format("%s-%s-%s", PREFIX, datePart, sequencePart);
    }
}