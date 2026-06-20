package com.facturemanagement.copy.domain.enums;

/**
 * Estados posibles de un trabajo de copia del módulo facture.
 * Replica el contrato uniforme del orquestador (ADR-38).
 */
public enum CopyEstado {

    INICIADO,
    EN_PROCESO,
    COMPLETADO,
    COMPLETADO_CON_ADVERTENCIAS,
    FALLIDO,
    ERROR_NO_REINTENTABLE,
    CANCELADO
}
