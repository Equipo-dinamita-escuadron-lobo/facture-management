package com.facturemanagement.copy.infraestructure.adapters.output.persistence.jpa;

import com.facturemanagement.copy.domain.enums.CopyEstado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para el log de idempotencia de copia de facture.
 * DDL MySQL: facture_copy_job_log con DATETIME(6) + UNIQUE (id_proceso, fase, modulo).
 * Usa LocalDateTime porque MySQL no soporta TIMESTAMPTZ nativo.
 * ADR-38.
 */
@Entity
@Table(name = "facture_copy_job_log",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_facture_copy_job_log",
                columnNames = {"id_proceso", "fase", "modulo"})
    },
    indexes = {
        @Index(name = "idx_facture_copy_job_log_id_proceso", columnList = "id_proceso")
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactureCopyJobLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_proceso", nullable = false, length = 36)
    private String idProceso;

    @Column(nullable = false)
    private Integer fase;

    @Column(nullable = false, length = 64)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CopyEstado estado;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "equivalencias_generadas")
    @Builder.Default
    private Integer equivalenciasGeneradas = 0;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
