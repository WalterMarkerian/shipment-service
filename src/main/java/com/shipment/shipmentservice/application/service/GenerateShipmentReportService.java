package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.response.FileResponse;
import com.shipment.shipmentservice.application.usecase.GenerateShipmentReportUseCase;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateShipmentReportService implements GenerateShipmentReportUseCase {

    private final ShipmentRepository repository;
    private final ReportExporter exporter; // Interfaz que definiremos abajo

    @Override
    @Transactional(readOnly = true)
    public FileResponse execute() {
        Map<ShipmentStatus, Long> stats = repository.countShipmentsByStatus();
        byte[] pdf = exporter.exportToPdf(stats);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "reporte_" + timestamp + ".pdf";

        return new FileResponse(pdf, fileName, "application/pdf");
    }
}