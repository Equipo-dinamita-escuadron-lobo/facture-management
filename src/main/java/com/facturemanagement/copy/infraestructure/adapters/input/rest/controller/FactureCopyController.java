package com.facturemanagement.copy.infraestructure.adapters.input.rest.controller;

import com.facturemanagement.copy.application.input.*;
import com.facturemanagement.copy.domain.exceptions.DuplicateCopyJobException;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del bounded context copy en facture-management.
 * Expone los 4 endpoints del contrato uniforme bajo /api/factures/copy.
 * ADR-38.
 */
@RestController
@RequestMapping("/api/factures/copy")
@RequiredArgsConstructor
@Slf4j
public class FactureCopyController {

    private final IExecuteFactureCopyPhasePort executePort;
    private final IGetFactureCopyStatusPort statusPort;
    private final ICancelFactureCopyPort cancelPort;
    private final ICleanupFactureCopyPort cleanupPort;

    /**
     * POST /api/factures/copy/phase
     * Ejecuta una fase del proceso de copia de facture.
     */
    @PostMapping("/phase")
    public ResponseEntity<CopyPhaseResponseDto> executePhase(
            @Valid @RequestBody CopyPhaseRequestDto request) {
        log.info("Ejecutando fase {} para proceso {} en facture", request.getFase(), request.getIdProceso());
        CopyPhaseResponseDto response = executePort.ejecutar(request);
        HttpStatus status = resolverHttpStatus(response.getEstado());
        return ResponseEntity.status(status).body(response);
    }

    /**
     * GET /api/factures/copy/{idProceso}/status
     */
    @GetMapping("/{idProceso}/status")
    public ResponseEntity<CopyStatusResponseDto> getStatus(
            @PathVariable String idProceso) {
        CopyStatusResponseDto response = statusPort.obtenerEstado(idProceso);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/factures/copy/{idProceso}/cancel
     */
    @PostMapping("/{idProceso}/cancel")
    public ResponseEntity<CopyCancelResponseDto> cancel(
            @PathVariable String idProceso) {
        CopyCancelResponseDto response = cancelPort.cancelar(idProceso);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/factures/copy/{idProceso}/cleanup
     */
    @DeleteMapping("/{idProceso}/cleanup")
    public ResponseEntity<Void> cleanup(
            @PathVariable String idProceso) {
        cleanupPort.limpiar(idProceso);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicateCopyJobException.class)
    public ResponseEntity<String> handleNotFound(DuplicateCopyJobException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private HttpStatus resolverHttpStatus(String estado) {
        if (estado == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (estado) {
            case "COMPLETADO", "COMPLETADO_CON_ADVERTENCIAS" -> HttpStatus.OK;
            case "ERROR_NO_REINTENTABLE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "ERROR_REINTENTABLE" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
    }
}
