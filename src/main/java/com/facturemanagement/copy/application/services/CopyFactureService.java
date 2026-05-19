package com.facturemanagement.copy.application.services;

import com.facturemanagement.copy.application.input.IExecuteFactureCopyPhasePort;
import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import com.facturemanagement.copy.application.output.IFactureSourceRepositoryPort;
import com.facturemanagement.copy.application.output.IFactureTargetRepositoryPort;
import com.facturemanagement.copy.domain.enums.CopyEstado;
import com.facturemanagement.copy.domain.models.CopyJobLog;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que orquesta la copia del módulo facture.
 *
 * Facture tiene relación M:M con ProductEntity (tabla FACTURES_AND_PRODUCTS).
 * Se remapea cada productId usando equivalenciasPrev tabla "product"
 * (Opción A: request rico, ADR-35/36).
 *
 * Si un productId no tiene equivalencia: se genera advertencia y se excluye
 * el producto (COMPLETADO_CON_ADVERTENCIAS).
 *
 * Base de datos: MySQL — entidad usa LocalDateTime.
 * Modelo de dominio usa Instant; la conversión está en el adaptador de persistencia.
 *
 * ADR-38.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CopyFactureService implements IExecuteFactureCopyPhasePort {

    private static final String MODULO = "facture";

    private final ICopyJobLogRepositoryPort logRepo;
    private final IFactureSourceRepositoryPort sourceRepo;
    private final IFactureTargetRepositoryPort targetRepo;

    @Override
    public CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request) {
        // Validación básica
        if (request.getEntOrigen().equals(request.getEntDestino())) {
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_NO_REINTENTABLE")
                    .mensaje("entOrigen y entDestino no pueden ser iguales")
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }

        String idProceso = request.getIdProceso().toString();

        // Idempotencia
        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("Fase {} del proceso {} ya fue ejecutada — retornando resultado previo (idempotencia)",
                    request.getFase(), idProceso);
            return construirResponseDesdeLog(previo.get());
        }

        // Registrar inicio
        CopyJobLog logInicio = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(CopyEstado.EN_PROCESO)
                .fechaInicio(Instant.now())
                .equivalenciasGeneradas(0)
                .build();
        logRepo.guardar(logInicio);

        // Construir índice productId para remap M:M
        Map<Long, Long> productIndex = construirIndiceProducto(request.getEquivalenciasPrev());

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();

        // Tenant override: forzar entDestino para que la persistencia use el tenant destino
        String tenantOriginal = TenantContext.getTenantId();
        TenantContext.setTenantId(request.getEntDestino());

        int totalRegistros = 0;

        try {
            List<FactureEntity> origenList = sourceRepo.findByEntOrigenBeforeSnapshot(
                    request.getEntOrigen(), request.getSnapshotCorte());

            for (FactureEntity original : origenList) {
                FactureEntity nueva = new FactureEntity();
                nueva.setFactId(null);
                nueva.setEntId(request.getEntDestino());
                nueva.setThId(original.getThId());
                nueva.setFactCode(null); // factCode es UNIQUE — se asigna null para auto-generación
                nueva.setFactObservations(original.getFactObservations());
                nueva.setFactureType(original.getFactureType());
                nueva.setFactSubtotals(original.getFactSubtotals());
                nueva.setFacSalesTax(original.getFacSalesTax());
                nueva.setFacWithholdingSource(original.getFacWithholdingSource());
                nueva.setDescounts(original.getDescounts());

                // Remapear productos M:M
                Set<ProductEntity> productosRemapeados = remapearProductos(
                        original.getFactId(), original.getFactProducts(),
                        productIndex, advertencias);
                nueva.setFactProducts(productosRemapeados);

                FactureEntity guardada = targetRepo.guardar(nueva);

                equivalencias.add(CopyEquivalenciaDto.builder()
                        .modulo(MODULO)
                        .tabla("facture")
                        .idViejo(String.valueOf(original.getFactId()))
                        .idNuevo(String.valueOf(guardada.getFactId()))
                        .build());
                totalRegistros++;
            }

        } catch (Exception e) {
            log.error("Error inesperado durante copia facture del proceso {}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error interno: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        } finally {
            if (tenantOriginal != null) {
                TenantContext.setTenantId(tenantOriginal);
            } else {
                TenantContext.clear();
            }
        }

        CopyEstado estadoFinal = advertencias.isEmpty()
                ? CopyEstado.COMPLETADO
                : CopyEstado.COMPLETADO_CON_ADVERTENCIAS;

        CopyJobLog logFin = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(estadoFinal)
                .fechaInicio(logInicio.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(equivalencias.size())
                .build();
        logRepo.guardar(logFin);

        return CopyPhaseResponseDto.builder()
                .estado(estadoFinal.name())
                .registrosProcesados(totalRegistros)
                .equivalenciasGeneradas(equivalencias)
                .mensaje("Copia facture completada exitosamente")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Construye índice productIdViejo → productIdNuevo desde equivalenciasPrev tabla "product".
     */
    private Map<Long, Long> construirIndiceProducto(List<CopyEquivalenciaDto> equivalenciasPrev) {
        if (equivalenciasPrev == null) return Collections.emptyMap();
        return equivalenciasPrev.stream()
                .filter(e -> "product".equals(e.getTabla())
                             && e.getIdViejo() != null && e.getIdNuevo() != null)
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getIdViejo()),
                        e -> Long.parseLong(e.getIdNuevo()),
                        (a, b) -> a));
    }

    /**
     * Remapea el conjunto de ProductEntity de una factura usando el índice de equivalencias.
     * Si un producto no tiene equivalencia: se registra advertencia y se excluye.
     */
    private Set<ProductEntity> remapearProductos(Long factureId,
                                                  Set<ProductEntity> productosOriginales,
                                                  Map<Long, Long> productIndex,
                                                  List<String> advertencias) {
        if (productosOriginales == null || productosOriginales.isEmpty()) {
            return new HashSet<>();
        }

        Set<ProductEntity> resultado = new HashSet<>();
        for (ProductEntity prod : productosOriginales) {
            Long idNuevo = productIndex.get(prod.getProductId());
            if (idNuevo == null) {
                String adv = String.format(
                    "Facture id=%s: productId=%s sin equivalencia en PRODUCTS; producto excluido.",
                    factureId, prod.getProductId());
                log.warn(adv);
                advertencias.add(adv);
                continue;
            }
            ProductEntity prodNuevo = new ProductEntity();
            prodNuevo.setProductId(idNuevo);
            prodNuevo.setCode(prod.getCode());
            prodNuevo.setDescount(prod.getDescount());
            prodNuevo.setAmount(prod.getAmount());
            prodNuevo.setDescription(prod.getDescription());
            prodNuevo.setVat(prod.getVat());
            prodNuevo.setUnitPrice(prod.getUnitPrice());
            prodNuevo.setSubtotal(prod.getSubtotal());
            resultado.add(prodNuevo);
        }
        return resultado;
    }

    private CopyPhaseResponseDto construirResponseDesdeLog(CopyJobLog log) {
        return CopyPhaseResponseDto.builder()
                .estado(log.getEstado().name())
                .registrosProcesados(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Resultado de ejecución previa (idempotencia)")
                .advertencias(Collections.emptyList())
                .build();
    }

    private void registrarFallo(CopyPhaseRequestDto request, String mensaje, Instant fechaInicio) {
        try {
            CopyJobLog logFallo = CopyJobLog.builder()
                    .idProceso(request.getIdProceso())
                    .fase(request.getFase())
                    .modulo(MODULO)
                    .estado(CopyEstado.FALLIDO)
                    .fechaInicio(fechaInicio != null ? fechaInicio : Instant.now())
                    .fechaFin(Instant.now())
                    .equivalenciasGeneradas(0)
                    .errorMessage(mensaje)
                    .build();
            logRepo.guardar(logFallo);
        } catch (Exception e) {
            log.error("Error al registrar fallo de copia facture: {}", e.getMessage());
        }
    }
}
