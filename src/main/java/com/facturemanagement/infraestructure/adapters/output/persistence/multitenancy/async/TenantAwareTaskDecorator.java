package com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.async;

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;

public class TenantAwareTaskDecorator implements TaskDecorator {

    /**
     * Decora un Runnable para hacerlo consciente del contexto de inquilino actual.
     * Esta función recupera el ID de inquilino actual de TenantContext y
     * crea un wrapper Runnable que establece el ID de inquilino antes de ejecutar
     * el Runnable original. Después de la ejecución, limpia el ID de inquilino para
     * evitar filtrar información de inquilino entre hilos.
     *
     * @param runnable el Runnable a ser decorado
     * @return un nuevo Runnable que asegura que el contexto de inquilino se
     *         establezca y se limpie
     */
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        String tenantId = TenantContext.getTenantId();
        return () -> {
            try {
                TenantContext.setTenantId(tenantId);
                runnable.run();
            } finally {
                TenantContext.setTenantId(null);
            }
        };
    }
}
