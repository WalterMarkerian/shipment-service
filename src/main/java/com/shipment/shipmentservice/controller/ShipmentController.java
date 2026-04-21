package com.shipment.shipmentservice.controller;

import com.shipment.shipmentservice.application.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.application.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.FileResponse;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.usecase.*;
import com.shipment.shipmentservice.common.constants.ApiPaths;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.SHIPMENTS)
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final CancelShipmentUseCase cancelShipmentUseCase;
    private final UpdateShipmentStatusUseCase updateShipmentStatusUseCase;
    private final GetShipmentByIdUseCase getByIdUseCase;
    private final GetShipmentByTrackingUseCase getByTrackingUseCase;
    private final FindShipmentsUseCase findUseCase;
    private final UpdateShipmentUseCase updateShipmentUseCase;
    private final GenerateShipmentReportUseCase generateShipmentReportUseCase;

    public ShipmentController(
            CreateShipmentUseCase createShipmentUseCase,
            CancelShipmentUseCase cancelShipmentUseCase,
            UpdateShipmentStatusUseCase updateShipmentStatusUseCase,
            GetShipmentByIdUseCase getByIdUseCase,
            GetShipmentByTrackingUseCase getByTrackingUseCase,
            FindShipmentsUseCase findUseCase,
            UpdateShipmentUseCase updateShipmentUseCase, GenerateShipmentReportUseCase generateShipmentReportUseCase) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.cancelShipmentUseCase = cancelShipmentUseCase;
        this.updateShipmentStatusUseCase = updateShipmentStatusUseCase;
        this.getByIdUseCase = getByIdUseCase;
        this.getByTrackingUseCase = getByTrackingUseCase;
        this.findUseCase = findUseCase;
        this.updateShipmentUseCase = updateShipmentUseCase;
        this.generateShipmentReportUseCase = generateShipmentReportUseCase;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createShipmentUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getAll(
            @ParameterObject ShipmentFilters filters,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(findUseCase.execute(filters, pageable));
    }

    @GetMapping("/seguimiento/{codigoSeguimiento}")
    public ResponseEntity<ShipmentResponse> getByTracking(@PathVariable String codigoSeguimiento) {
        return ResponseEntity.ok(getByTrackingUseCase.execute(codigoSeguimiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(updateShipmentUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ShipmentResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(cancelShipmentUseCase.execute(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(updateShipmentStatusUseCase.execute(id, status));
    }

    @GetMapping("/reporte")
    public ResponseEntity<byte[]> getReport() {
        FileResponse report = generateShipmentReportUseCase.execute();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.fileName() + "\"")
                .body(report.content());
    }

}