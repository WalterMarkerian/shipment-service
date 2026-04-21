package com.shipment.shipmentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipment.shipmentservice.application.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.application.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.application.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.application.usecase.*;
import com.shipment.shipmentservice.domain.exception.InvalidShipmentStateException;
import com.shipment.shipmentservice.domain.exception.ShipmentNotFoundException;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import com.shipment.shipmentservice.domain.model.ShipmentType;
import com.shipment.shipmentservice.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integracion de la capa HTTP del ShipmentController.
 *
 * Carga unicamente la capa web (sin base de datos ni filtros de seguridad).
 * Los casos de uso se simulan con mocks.
 * La identidad del usuario se inyecta directamente con @WithMockUser.
 */
@WebMvcTest(com.shipment.shipmentservice.controller.ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ShipmentController - Integracion HTTP")
class ShipmentControllerIT {

    private static final String BASE_URL = "/api/v1/shipments";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CreateShipmentUseCase createShipmentUseCase;
    @MockBean private CancelShipmentUseCase cancelShipmentUseCase;
    @MockBean private UpdateShipmentStatusUseCase updateShipmentStatusUseCase;
    @MockBean private GetShipmentByIdUseCase getByIdUseCase;
    @MockBean private GetShipmentByTrackingUseCase getByTrackingUseCase;
    @MockBean private FindShipmentsUseCase findUseCase;
    @MockBean private UpdateShipmentUseCase updateShipmentUseCase;
    @MockBean private GenerateShipmentReportUseCase generateShipmentReportUseCase;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthenticationProvider authenticationProvider;

    // POST

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /shipments: ADMIN con datos validos -> 201 Created")
    void createShipment_asAdmin_shouldReturn201() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest(
                "Juan Perez", "Av. Santa Fe 1234", "CABA", "Buenos Aires", "1425", ShipmentType.ESTANDAR);
        when(createShipmentUseCase.execute(any())).thenReturn(buildResponse(UUID.randomUUID(), ShipmentStatus.PENDING));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.recipientName").value("Juan Perez"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /shipments: nombre en blanco -> 400 VALIDATION_ERROR")
    void createShipment_blankRecipientName_shouldReturn400() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest(
                "", "Av. Santa Fe 1234", "CABA", "Buenos Aires", "1425", ShipmentType.ESTANDAR);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /shipments: codigo postal invalido -> 400 VALIDATION_ERROR")
    void createShipment_invalidPostalCode_shouldReturn400() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest(
                "Juan Perez", "Av. Santa Fe 1234", "CABA", "Buenos Aires", "ABCD", ShipmentType.ESTANDAR);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /shipments: servicio lanza excepcion inesperada -> 500")
    void createShipment_unexpectedError_shouldReturn500() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest(
                "Juan Perez", "Av. Santa Fe 1234", "CABA", "Buenos Aires", "1425", ShipmentType.ESTANDAR);
        when(createShipmentUseCase.execute(any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    // GET all

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /shipments: USER -> 200 OK con lista paginada")
    void getAllShipments_asUser_shouldReturn200() throws Exception {
        Page<ShipmentResponse> page = new PageImpl<>(List.of(
                buildResponse(UUID.randomUUID(), ShipmentStatus.PENDING),
                buildResponse(UUID.randomUUID(), ShipmentStatus.IN_TRANSIT)));
        when(findUseCase.execute(any(), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /shipments: lista vacia -> 200 OK con pagina vacia")
    void getAllShipments_emptyList_shouldReturn200() throws Exception {
        when(findUseCase.execute(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // GET by ID

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /shipments/{id}: ID existente -> 200 OK")
    void getById_existingId_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(getByIdUseCase.execute(id)).thenReturn(buildResponse(id, ShipmentStatus.PENDING));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /shipments/{id}: ID inexistente -> 404 NOT_FOUND")
    void getById_nonExistingId_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(getByIdUseCase.execute(id)).thenThrow(new ShipmentNotFoundException("No encontrado: " + id));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // GET by tracking

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /shipments/seguimiento/{codigo}: codigo existente -> 200 OK")
    void getByTracking_existingCode_shouldReturn200() throws Exception {
        String code = "ENV-20260421-00001";
        when(getByTrackingUseCase.execute(code)).thenReturn(buildResponse(UUID.randomUUID(), ShipmentStatus.IN_TRANSIT));

        mockMvc.perform(get(BASE_URL + "/seguimiento/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingCode").value(code));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /shipments/seguimiento/{codigo}: codigo inexistente -> 404")
    void getByTracking_nonExistingCode_shouldReturn404() throws Exception {
        String code = "ENV-99990101-99999";
        when(getByTrackingUseCase.execute(code))
                .thenThrow(new ShipmentNotFoundException("No encontrado: " + code));

        mockMvc.perform(get(BASE_URL + "/seguimiento/{code}", code))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // PUT

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /shipments/{id}: actualizacion valida -> 200 OK")
    void updateShipment_validRequest_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateShipmentRequest req = new UpdateShipmentRequest(
                "Nuevo Nombre", "Nueva Direccion", "Nueva Ciudad", "Nueva Provincia", "9999");
        when(updateShipmentUseCase.execute(eq(id), any())).thenReturn(buildResponse(id, ShipmentStatus.PENDING));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /shipments/{id}: envio ENTREGADO -> 400 INVALID_STATE")
    void updateShipment_deliveredShipment_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateShipmentRequest req = new UpdateShipmentRequest(
                "Nuevo Nombre", "Nueva Direccion", "Nueva Ciudad", "Nueva Provincia", "9999");
        when(updateShipmentUseCase.execute(eq(id), any()))
                .thenThrow(new InvalidShipmentStateException("No se puede modificar un envio en estado DELIVERED"));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /shipments/{id}: campos vacios -> 400 VALIDATION_ERROR")
    void updateShipment_emptyFields_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateShipmentRequest req = new UpdateShipmentRequest("", "", "", "", "");

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // DELETE

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /shipments/{id}: envio PENDING -> 200 OK (baja logica)")
    void cancelShipment_pendingShipment_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelShipmentUseCase.execute(id)).thenReturn(buildResponse(id, ShipmentStatus.CANCELLED));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /shipments/{id}: envio EN_TRANSITO -> 400 INVALID_STATE")
    void cancelShipment_inTransitShipment_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelShipmentUseCase.execute(id))
                .thenThrow(new InvalidShipmentStateException("Solo los envios PENDIENTES pueden cancelarse."));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /shipments/{id}: ID inexistente -> 404")
    void cancelShipment_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelShipmentUseCase.execute(id))
                .thenThrow(new ShipmentNotFoundException("No encontrado: " + id));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // PATCH

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /shipments/{id}/estado: PENDING -> IN_TRANSIT -> 200 OK")
    void updateStatus_pendingToInTransit_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateShipmentStatusUseCase.execute(id, ShipmentStatus.IN_TRANSIT))
                .thenReturn(buildResponse(id, ShipmentStatus.IN_TRANSIT));

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", id)
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /shipments/{id}/estado: IN_TRANSIT -> DELIVERED -> 200 OK")
    void updateStatus_inTransitToDelivered_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateShipmentStatusUseCase.execute(id, ShipmentStatus.DELIVERED))
                .thenReturn(buildResponse(id, ShipmentStatus.DELIVERED));

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", id)
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /shipments/{id}/estado: transicion invalida -> 400 INVALID_STATE")
    void updateStatus_invalidTransition_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateShipmentStatusUseCase.execute(id, ShipmentStatus.IN_TRANSIT))
                .thenThrow(new InvalidShipmentStateException("Transicion invalida."));

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", id)
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /shipments/{id}/estado: estado invalido -> 500 (enum incompatible, no manejado)")
    void updateStatus_invalidStatusValue_shouldReturn500() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", id)
                        .param("status", "ESTADO_INVALIDO"))
                .andExpect(status().is5xxServerError());
    }

    private ShipmentResponse buildResponse(UUID id, ShipmentStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new ShipmentResponse(id, "ENV-20260421-00001", status,
                "Juan Perez", "Av. Santa Fe 1234", "CABA",
                "Buenos Aires", "1425", ShipmentType.ESTANDAR, now, now);
    }
}