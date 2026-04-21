package com.shipment.shipmentservice.domain.repository;

import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID id);
    Optional<Shipment> findByTrackingCode(String trackingCode);
    boolean existsByTrackingCode(String trackingCode);
    Map<ShipmentStatus, Long> countShipmentsByStatus();
    Page<Shipment> findAll(ShipmentFilters filters, Pageable pageable);
    Long getNextSequenceValue();

}