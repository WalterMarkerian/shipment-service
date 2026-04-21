package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.request.ShipmentFilters;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.FindShipmentsUseCase;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindShipmentsService implements FindShipmentsUseCase {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> execute(ShipmentFilters filters, Pageable pageable) {
        return repository.findAll(filters, pageable)
                .map(mapper::toResponse);
    }
}