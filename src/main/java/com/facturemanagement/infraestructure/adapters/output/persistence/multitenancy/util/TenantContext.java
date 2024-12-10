package com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {
    private TenantContext() {
    }

    private static final InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

    /**
     * Establece el ID de inquilino para el hilo actual. Este valor es utilizado
     * por el {@link TenantIdentifierResolver} para resolver el ID de inquilino
     * para operaciones de base de datos.
     *
     * @param tenantId el ID de inquilino a establecer
     */
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenantId to " + tenantId);
        currentTenant.set(tenantId);
    }

    /**
     * Obtiene el ID de inquilino actual. Este valor se establece utilizando
     * {@link #setTenantId(String)} y se utiliza por el
     * {@link TenantIdentifierResolver} para resolver el ID de inquilino para
     * operaciones de base de datos.
     *
     * @return el ID de inquilino actual
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * Limpia el contexto de inquilino actual. Despu s de esta llamada, el
     * valor de inquilino actual es null.
     */
    public static void clear() {
        currentTenant.remove();
    }
}
