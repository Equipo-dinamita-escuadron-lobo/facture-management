package com.facturemanagement.copy.infraestructure.adapters.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.facturemanagement.copy.application.input.*;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.controller.FactureCopyController;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del controlador de copia de facture.
 * Standalone MockMvc — sin contexto Spring completo.
 * RED phase: Fase 7.5.
 */
@ExtendWith(MockitoExtension.class)
class FactureCopyControllerTest {

    @Mock private IExecuteFactureCopyPhasePort executePort;
    @Mock private IGetFactureCopyStatusPort statusPort;
    @Mock private ICancelFactureCopyPort cancelPort;
    @Mock private ICleanupFactureCopyPort cleanupPort;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FactureCopyController controller =
                new FactureCopyController(executePort, statusPort, cancelPort, cleanupPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ----------------------------------------------------------------
    // POST /api/factures/copy/phase
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POST /api/factures/copy/phase retorna 200 con COMPLETADO")
    void executePhase_retorna200() throws Exception {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(1)
                .entOrigen("ENT_A")
                .entDestino("ENT_B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(List.of())
                .build();

        CopyPhaseResponseDto response = CopyPhaseResponseDto.builder()
                .estado("COMPLETADO")
                .registrosProcesados(3)
                .equivalenciasGeneradas(List.of())
                .mensaje("Copia facture completada")
                .advertencias(List.of())
                .build();

        when(executePort.ejecutar(any())).thenReturn(response);

        mockMvc.perform(post("/api/factures/copy/phase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.registrosProcesados").value(3));
    }

    // ----------------------------------------------------------------
    // GET /api/factures/copy/{idProceso}/status
    // ----------------------------------------------------------------

    @Test
    @DisplayName("GET /api/factures/copy/{id}/status retorna 200 con estado")
    void getStatus_retorna200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        CopyStatusResponseDto response = CopyStatusResponseDto.builder()
                .fase(1)
                .estado("COMPLETADO")
                .registrosProcesados(5)
                .intentos(1)
                .build();

        when(statusPort.obtenerEstado(idProceso)).thenReturn(response);

        mockMvc.perform(get("/api/factures/copy/{idProceso}/status", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.fase").value(1));
    }

    // ----------------------------------------------------------------
    // POST /api/factures/copy/{idProceso}/cancel
    // ----------------------------------------------------------------

    @Test
    @DisplayName("POST /api/factures/copy/{id}/cancel retorna 200 con CANCELADO")
    void cancel_retorna200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        CopyCancelResponseDto response = CopyCancelResponseDto.builder()
                .estado("CANCELADO")
                .mensaje("Proceso cancelado")
                .build();

        when(cancelPort.cancelar(idProceso)).thenReturn(response);

        mockMvc.perform(post("/api/factures/copy/{idProceso}/cancel", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    // ----------------------------------------------------------------
    // DELETE /api/factures/copy/{idProceso}/cleanup
    // ----------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/factures/copy/{id}/cleanup retorna 204")
    void cleanup_retorna204() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/factures/copy/{idProceso}/cleanup", idProceso))
                .andExpect(status().isNoContent());
    }
}
