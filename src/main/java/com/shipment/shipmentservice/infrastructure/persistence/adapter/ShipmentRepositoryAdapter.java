package com.shipment.shipmentservice.infrastructure.persistence.adapter;

import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import com.shipment.shipmentservice.infrastructure.persistence.entity.ShipmentEntity;
import com.shipment.shipmentservice.infrastructure.persistence.mapper.ShipmentPersistenceMapper;
import com.shipment.shipmentservice.infrastructure.persistence.repository.JpaShipmentRepository;
import com.shipment.shipmentservice.infrastructure.persistence.specifications.ShipmentSpecifications;
import jakarta.persistence.EntityManager; // Importante
import jakarta.persistence.PersistenceContext; // Importante
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ShipmentRepositoryAdapter implements ShipmentRepository {

    private final JpaShipmentRepository jpaRepository;
    private final ShipmentPersistenceMapper mapper;

    private final EntityManager entityManager;

    public ShipmentRepositoryAdapter(JpaShipmentRepository jpaRepository,
                                     ShipmentPersistenceMapper mapper,
                                     EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentEntity entity = mapper.toEntity(shipment);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingCode(String trackingCode) {
        return jpaRepository.findByTrackingCode(trackingCode).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTrackingCode(String trackingCode) {
        return jpaRepository.existsByTrackingCode(trackingCode);
    }

    @Override
    public Page<Shipment> findAll(ShipmentFilters filters, Pageable pageable) {
        Specification<ShipmentEntity> spec = ShipmentSpecifications.withFilters(filters);
        return jpaRepository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Long getNextSequenceValue() {
        return ((Number) entityManager.createNativeQuery("SELECT nextval('shipment_tracking_seq')")
                .getSingleResult()).longValue();    }

    @Override
    public Map<ShipmentStatus, Long> countShipmentsByStatus() {
        return jpaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        ShipmentEntity::getStatus,
                        Collectors.counting()
                ));
    }
}