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
import com.facturemanagement.domain.model.eFactureType;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Override
    public CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request) {
        // Modo RESTORE: datosImportados presente → importar desde JSON
        if (request.getDatosImportados() != null) {
            return ejecutarImportacion(request);
        }
        // Modo BACKUP: entDestino ausente → exportar datos de la empresa origen
        if (request.getEntDestino() == null || request.getEntDestino().isBlank()) {
            return ejecutarExportacion(request);
        }
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

                FactureEntity guardada = targetRepo.guardarConProductos(nueva);

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

    // ----------------------------------------------------------------
    // BACKUP: exportar datos de la empresa origen como JSON serializable
    // ----------------------------------------------------------------

    private CopyPhaseResponseDto ejecutarExportacion(CopyPhaseRequestDto request) {
        log.info("Modo BACKUP — exportando facturas de entOrigen={}", request.getEntOrigen());

        try {
            List<FactureEntity> facturas = sourceRepo.findByEntOrigenBeforeSnapshot(
                    request.getEntOrigen(), request.getSnapshotCorte());

            List<Map<String, Object>> exportados = new ArrayList<>();
            for (FactureEntity f : facturas) {
                Map<String, Object> factMap = new LinkedHashMap<>();
                factMap.put("factId", f.getFactId());
                factMap.put("entId", f.getEntId());
                factMap.put("thId", f.getThId());
                factMap.put("factCode", f.getFactCode());
                factMap.put("factObservations", f.getFactObservations());
                factMap.put("factureType", f.getFactureType() != null ? f.getFactureType().name() : null);
                factMap.put("factSubtotals", f.getFactSubtotals());
                factMap.put("facSalesTax", f.getFacSalesTax());
                factMap.put("facWithholdingSource", f.getFacWithholdingSource());
                factMap.put("descounts", f.getDescounts());

                List<Map<String, Object>> productos = new ArrayList<>();
                if (f.getFactProducts() != null) {
                    for (ProductEntity p : f.getFactProducts()) {
                        Map<String, Object> prodMap = new LinkedHashMap<>();
                        prodMap.put("productId", p.getProductId());
                        prodMap.put("code", p.getCode());
                        prodMap.put("descount", p.getDescount());
                        prodMap.put("amount", p.getAmount());
                        prodMap.put("description", p.getDescription());
                        prodMap.put("vat", p.getVat());
                        prodMap.put("unitPrice", p.getUnitPrice());
                        prodMap.put("subtotal", p.getSubtotal());
                        productos.add(prodMap);
                    }
                }
                factMap.put("productos", productos);
                exportados.add(factMap);
            }

            log.info("BACKUP completado — {} facturas exportadas de entOrigen={}", exportados.size(), request.getEntOrigen());

            return CopyPhaseResponseDto.builder()
                    .estado("COMPLETADO")
                    .registrosProcesados(exportados.size())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .mensaje("Modo BACKUP — " + exportados.size() + " facturas exportadas")
                    .advertencias(Collections.emptyList())
                    .datosExportados(exportados)
                    .build();

        } catch (Exception e) {
            log.error("Error durante BACKUP facture de entOrigen={}: {}", request.getEntOrigen(), e.getMessage(), e);
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error en BACKUP: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }
    }

    // ----------------------------------------------------------------
    // RESTORE: importar datos desde JSON, insertar en empresa destino
    // ----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private CopyPhaseResponseDto ejecutarImportacion(CopyPhaseRequestDto request) {
        String idProceso = request.getIdProceso().toString();
        log.info("Modo RESTORE — importando facturas al proceso={}, entDestino={}", idProceso, request.getEntDestino());

        // Idempotencia
        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("RESTORE fase {} del proceso {} ya fue ejecutada — idempotencia", request.getFase(), idProceso);
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

        List<Map<String, Object>> facturasList;
        try {
            facturasList = objectMapper.convertValue(request.getDatosImportados(), List.class);
        } catch (Exception e) {
            log.error("datosImportados no tiene el formato esperado (List<Map>): {}", e.getMessage());
            registrarFallo(request, "datosImportados inválido: " + e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_NO_REINTENTABLE")
                    .mensaje("datosImportados inválido: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }

        // Construir índice de equivalencias de productos y thirds
        List<CopyEquivalenciaDto> equivPrev = request.getEquivalenciasPrev() != null
                ? request.getEquivalenciasPrev()
                : Collections.emptyList();

        Map<Long, Long> productIndex = construirIndiceProducto(equivPrev);
        Map<Long, Long> thirdIndex = construirIndiceTercero(equivPrev);

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();
        int totalRegistros = 0;

        String tenantOriginal = TenantContext.getTenantId();

        try {
            for (Map<String, Object> factMap : facturasList) {
                Long factIdOriginal = toLong(factMap.get("factId"));

                // Remapear FK a thirds
                Long thIdOriginal = toLong(factMap.get("thId"));
                Long thIdNuevo = remapearFkPrev(thIdOriginal, "third", equivPrev, advertencias, factIdOriginal);

                // Construir entidad factura
                FactureEntity nueva = new FactureEntity();
                nueva.setFactId(null);
                nueva.setEntId(request.getEntDestino());
                nueva.setThId(thIdNuevo);
                nueva.setFactCode(null); // UNIQUE — se deja null para auto-generación
                nueva.setFactObservations(toStr(factMap.get("factObservations")));

                String typeStr = toStr(factMap.get("factureType"));
                if (typeStr != null) {
                    try {
                        nueva.setFactureType(eFactureType.valueOf(typeStr));
                    } catch (IllegalArgumentException ex) {
                        advertencias.add("Factura id=" + factIdOriginal + ": factureType desconocido '" + typeStr + "', se ignora");
                    }
                }

                nueva.setFactSubtotals(toDbl(factMap.get("factSubtotals")));
                nueva.setFacSalesTax(toDbl(factMap.get("facSalesTax")));
                nueva.setFacWithholdingSource(toDbl(factMap.get("facWithholdingSource")));
                nueva.setDescounts(toDbl(factMap.get("descounts")));

                // Remapear productos M:N
                List<Map<String, Object>> productosRaw = (List<Map<String, Object>>) factMap.get("productos");
                Set<ProductEntity> productosRemapeados = new HashSet<>();
                if (productosRaw != null) {
                    for (Map<String, Object> prodMap : productosRaw) {
                        Long prodIdOriginal = toLong(prodMap.get("productId"));
                        Long prodIdNuevo = productIndex.get(prodIdOriginal);
                        if (prodIdNuevo == null) {
                            String adv = String.format(
                                "Factura id=%s: productId=%s sin equivalencia en PRODUCTS; producto excluido.",
                                factIdOriginal, prodIdOriginal);
                            log.warn(adv);
                            advertencias.add(adv);
                            continue;
                        }
                        ProductEntity prodNuevo = new ProductEntity();
                        prodNuevo.setProductId(prodIdNuevo);
                        prodNuevo.setCode(toStr(prodMap.get("code")));
                        prodNuevo.setDescount(toDbl(prodMap.get("descount")));
                        prodNuevo.setAmount(toDbl(prodMap.get("amount")));
                        prodNuevo.setDescription(toStr(prodMap.get("description")));
                        prodNuevo.setVat(toDbl(prodMap.get("vat")));
                        prodNuevo.setUnitPrice(toDbl(prodMap.get("unitPrice")));
                        prodNuevo.setSubtotal(toDbl(prodMap.get("subtotal")));
                        productosRemapeados.add(prodNuevo);
                    }
                }
                nueva.setFactProducts(productosRemapeados);

                FactureEntity guardada = targetRepo.guardarConProductos(nueva);

                equivalencias.add(CopyEquivalenciaDto.builder()
                        .modulo(MODULO)
                        .tabla("facture")
                        .idViejo(String.valueOf(factIdOriginal))
                        .idNuevo(String.valueOf(guardada.getFactId()))
                        .build());
                totalRegistros++;
            }

        } catch (Exception e) {
            log.error("Error inesperado durante RESTORE facture proceso={}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error interno RESTORE: " + e.getMessage())
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
                .mensaje("RESTORE facture completado")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Índice de equivalencias de terceros
    // ----------------------------------------------------------------

    private Map<Long, Long> construirIndiceTercero(List<CopyEquivalenciaDto> equivalenciasPrev) {
        if (equivalenciasPrev == null) return Collections.emptyMap();
        return equivalenciasPrev.stream()
                .filter(e -> "third".equals(e.getTabla())
                             && e.getIdViejo() != null && e.getIdNuevo() != null)
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getIdViejo()),
                        e -> Long.parseLong(e.getIdNuevo()),
                        (a, b) -> a));
    }

    // ----------------------------------------------------------------
    // Remap de FK cross-service desde equivalenciasPrev
    // ----------------------------------------------------------------

    private Long remapearFkPrev(Long idViejo, String tabla,
                                 List<CopyEquivalenciaDto> equivPrev,
                                 List<String> advertencias,
                                 Long entidadId) {
        if (idViejo == null) return null;
        return equivPrev.stream()
                .filter(e -> tabla.equals(e.getTabla())
                             && e.getIdViejo() != null
                             && Long.parseLong(e.getIdViejo()) == idViejo)
                .map(e -> Long.parseLong(e.getIdNuevo()))
                .findFirst()
                .orElseGet(() -> {
                    advertencias.add("FK missing: tabla=" + tabla + " id=" + idViejo
                            + " (factura id=" + entidadId + ")");
                    return null;
                });
    }

    // ----------------------------------------------------------------
    // Helpers de conversión de tipos (Object → primitivo)
    // ----------------------------------------------------------------

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        return null;
    }

    private String toStr(Object v) {
        return v != null ? v.toString() : null;
    }

    private boolean toBool(Object v) {
        return v instanceof Boolean b && b;
    }

    private double toDbl(Object v) {
        if (v instanceof Double d) return d;
        if (v instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
