package com.facturemanagement.copy.application;

import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import com.facturemanagement.copy.application.output.IFactureSourceRepositoryPort;
import com.facturemanagement.copy.application.output.IFactureTargetRepositoryPort;
import com.facturemanagement.copy.application.services.CopyFactureService;
import com.facturemanagement.copy.domain.enums.CopyEstado;
import com.facturemanagement.copy.domain.models.CopyJobLog;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CopyFactureService.
 * Mockito standalone — sin contexto Spring.
 * RED phase: Fase 7.1.
 */
@ExtendWith(MockitoExtension.class)
class FactureCopyServiceTest {

    @Mock
    private ICopyJobLogRepositoryPort logRepo;

    @Mock
    private IFactureSourceRepositoryPort sourceRepo;

    @Mock
    private IFactureTargetRepositoryPort targetRepo;

    private CopyFactureService service;

    @BeforeEach
    void setUp() {
        service = new CopyFactureService(logRepo, sourceRepo, targetRepo, new com.fasterxml.jackson.databind.ObjectMapper());
    }

    // ----------------------------------------------------------------
    // Escenario 1: copia 2 facturas de origen → destino (COMPLETADO)
    // ----------------------------------------------------------------

    @Test
    @DisplayName("copia 2 facturas de tenant origen a tenant destino con estado COMPLETADO")
    void copiar_dosFacturas_retornaCompletado() {
        // Arrange
        UUID idProceso = UUID.randomUUID();
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(1)
                .entOrigen("ENT_A")
                .entDestino("ENT_B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(List.of())
                .build();

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 1))
                .thenReturn(Optional.empty());

        FactureEntity f1 = new FactureEntity();
        f1.setFactId(10L);
        f1.setEntId("ENT_A");
        f1.setThId(1L);
        f1.setFactCode(1001L);
        f1.setFactProducts(new HashSet<>());

        FactureEntity f2 = new FactureEntity();
        f2.setFactId(20L);
        f2.setEntId("ENT_A");
        f2.setThId(2L);
        f2.setFactCode(1002L);
        f2.setFactProducts(new HashSet<>());

        when(sourceRepo.findByEntOrigenBeforeSnapshot(eq("ENT_A"), any()))
                .thenReturn(List.of(f1, f2));

        FactureEntity guardada1 = new FactureEntity();
        guardada1.setFactId(100L);
        FactureEntity guardada2 = new FactureEntity();
        guardada2.setFactId(200L);

        when(targetRepo.guardar(any())).thenReturn(guardada1, guardada2);

        // Act
        CopyPhaseResponseDto response = service.ejecutar(request);

        // Assert
        assertThat(response.getEstado()).isEqualTo("COMPLETADO");
        assertThat(response.getRegistrosProcesados()).isEqualTo(2);
        assertThat(response.getEquivalenciasGeneradas()).hasSize(2);
        verify(logRepo, times(2)).guardar(any());
    }

    // ----------------------------------------------------------------
    // Escenario 2: idempotencia — retorna resultado previo si ya existe
    // ----------------------------------------------------------------

    @Test
    @DisplayName("idempotencia: retorna resultado previo si ya existe log para el proceso")
    void copiar_idempotencia_retornaResultadoPrevio() {
        // Arrange
        UUID idProceso = UUID.randomUUID();
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(1)
                .entOrigen("ENT_A")
                .entDestino("ENT_B")
                .snapshotCorte(Instant.now())
                .build();

        CopyJobLog logPrevio = CopyJobLog.builder()
                .idProceso(idProceso)
                .fase(1)
                .modulo("facture")
                .estado(CopyEstado.COMPLETADO)
                .equivalenciasGeneradas(3)
                .build();

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 1))
                .thenReturn(Optional.of(logPrevio));

        // Act
        CopyPhaseResponseDto response = service.ejecutar(request);

        // Assert
        assertThat(response.getEstado()).isEqualTo("COMPLETADO");
        assertThat(response.getMensaje()).contains("idempotencia");
        verify(sourceRepo, never()).findByEntOrigenBeforeSnapshot(any(), any());
        verify(targetRepo, never()).guardar(any());
    }

    // ----------------------------------------------------------------
    // Escenario 3: origen == destino → ERROR_NO_REINTENTABLE
    // ----------------------------------------------------------------

    @Test
    @DisplayName("origen igual a destino retorna ERROR_NO_REINTENTABLE sin consultar BD")
    void copiar_origenIgualDestino_retornaErrorNoReintentable() {
        // Arrange
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(1)
                .entOrigen("MISMA_ENT")
                .entDestino("MISMA_ENT")
                .snapshotCorte(Instant.now())
                .build();

        // Act
        CopyPhaseResponseDto response = service.ejecutar(request);

        // Assert
        assertThat(response.getEstado()).isEqualTo("ERROR_NO_REINTENTABLE");
        verify(logRepo, never()).guardar(any());
        verify(sourceRepo, never()).findByEntOrigenBeforeSnapshot(any(), any());
    }

    // ----------------------------------------------------------------
    // Escenario 4: factura con productos — remap M:M vía equivalenciasPrev "product"
    // ----------------------------------------------------------------

    @Test
    @DisplayName("copia factura con productos: productId remapeado via equivalenciasPrev tabla 'product'")
    void copiar_facturaConProductos_remapeaProductIds() {
        // Arrange
        UUID idProceso = UUID.randomUUID();

        CopyEquivalenciaDto eq = CopyEquivalenciaDto.builder()
                .modulo("products")
                .tabla("product")
                .idViejo("55")
                .idNuevo("155")
                .build();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(1)
                .entOrigen("ENT_A")
                .entDestino("ENT_B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(List.of(eq))
                .build();

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 1))
                .thenReturn(Optional.empty());

        // Factura con producto 55
        ProductEntity producto = new ProductEntity();
        producto.setProductId(55L);

        FactureEntity f1 = new FactureEntity();
        f1.setFactId(10L);
        f1.setEntId("ENT_A");
        f1.setFactCode(1001L);
        f1.setFactProducts(Set.of(producto));

        when(sourceRepo.findByEntOrigenBeforeSnapshot(eq("ENT_A"), any()))
                .thenReturn(List.of(f1));

        FactureEntity guardada = new FactureEntity();
        guardada.setFactId(110L);

        ArgumentCaptor<FactureEntity> captor = ArgumentCaptor.forClass(FactureEntity.class);
        when(targetRepo.guardar(captor.capture())).thenReturn(guardada);

        // Act
        CopyPhaseResponseDto response = service.ejecutar(request);

        // Assert
        assertThat(response.getEstado()).isEqualTo("COMPLETADO");
        FactureEntity guardadaCapturada = captor.getValue();
        // El producto guardado debe tener productId=155 (remapeado)
        assertThat(guardadaCapturada.getFactProducts())
                .extracting(ProductEntity::getProductId)
                .containsExactly(155L);
    }
}
