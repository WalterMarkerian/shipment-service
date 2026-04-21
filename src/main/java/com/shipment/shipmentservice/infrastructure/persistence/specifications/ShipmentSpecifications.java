package com.shipment.shipmentservice.infrastructure.persistence.specifications;

import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ShipmentSpecifications {

    public static Specification<ShipmentEntity> withFilters(ShipmentFilters filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }

            if (filters.type() != null) {
                predicates.add(cb.equal(root.get("type"), filters.type()));
            }

            if (filters.destinationCity() != null && !filters.destinationCity().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("destinationCity")),
                        "%" + filters.destinationCity().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}