package com.shipment.shipmentservice.infrastructure.persistence.repository;

import com.shipment.shipmentservice.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface JpaShipmentRepository extends JpaRepository<ShipmentEntity, UUID>, JpaSpecificationExecutor<ShipmentEntity> {
    Optional<ShipmentEntity> findByTrackingCode(String trackingCode);
    boolean existsByTrackingCode(String trackingCode);

}