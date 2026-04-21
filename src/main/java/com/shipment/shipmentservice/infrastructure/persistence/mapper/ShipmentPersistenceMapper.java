package com.shipment.shipmentservice.infrastructure.persistence.mapper;

import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.infrastructure.persistence.entity.ShipmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentPersistenceMapper {

    ShipmentEntity toEntity(Shipment shipment);

    default Shipment toDomain(ShipmentEntity entity) {
        return Shipment.restore(
                entity.getId(),
                entity.getTrackingCode(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getRecipientName(),
                entity.getDestinationAddress(),
                entity.getDestinationCity(),
                entity.getDestinationProvince(),
                entity.getPostalCode(),
                entity.getType(),
                entity.getUpdatedAt(),
                entity.isActive()
        );
    }
}