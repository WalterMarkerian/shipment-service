package com.shipment.shipmentservice.application.service;

import com.shipment.shipmentservice.application.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.mapper.ShipmentMapper;
import com.shipment.shipmentservice.application.usecase.CreateShipmentUseCase;
import com.shipment.shipmentservice.domain.model.Shipment;
import com.shipment.shipmentservice.domain.repository.ShipmentRepository;
import com.shipment.shipmentservice.domain.service.TrackingCodeGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateShipmentService implements CreateShipmentUseCase {

    private final ShipmentRepository shipmentRepository;
    private final TrackingCodeGenerator trackingCodeGenerator;
    private final ShipmentMapper shipmentMapper;

    public CreateShipmentService(ShipmentRepository shipmentRepository,
                                 TrackingCodeGenerator trackingCodeGenerator,
                                 ShipmentMapper shipmentMapper) {
        this.shipmentRepository = shipmentRepository;
        this.trackingCodeGenerator = trackingCodeGenerator;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    @Transactional
    public ShipmentResponse execute(CreateShipmentRequest request) {

        int maxRetries = 3;

        for (int i = 0; i < maxRetries; i++) {

            String trackingCode = trackingCodeGenerator.generate();

            Shipment shipment = Shipment.create(
                    trackingCode,
                    request.recipientName(),
                    request.destinationAddress(),
                    request.destinationCity(),
                    request.destinationProvince(),
                    request.postalCode(),
                    request.type()
            );

            try {
                Shipment saved = shipmentRepository.save(shipment);

                return shipmentMapper.toResponse(saved);

            } catch (DataIntegrityViolationException ex) {

                if (isDuplicateTrackingCode(ex)) {
                    continue;
                }

                throw ex;
            }
        }

        throw new RuntimeException("No se pudo generar un trackingCode único");
    }

    private boolean isDuplicateTrackingCode(DataIntegrityViolationException ex) {
        return ex.getMostSpecificCause() != null &&
                ex.getMostSpecificCause().getMessage().contains("tracking_code");
    }
}