package com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.interceptor;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Autowired
    private IJwtUtils jwtUtils;

    /**
     * Antes de manejar la solicitud, establece el inquilino actual en el
     * contexto local de hilo.
     * 
     * @param request la solicitud actual
     * @throws Exception si ocurre un error al establecer el inquilino
     */
    @Override
    public void preHandle(WebRequest request) throws Exception {
        TenantContext.setTenantId(jwtUtils.getId());
    }

    /**
     * Limpiar el contexto del inquilino después de manejar la solicitud.
     *
     * @param request la solicitud actual
     * @param model   el modelo utilizado por el controlador
     * @throws Exception si ocurre un error al limpiar el contexto del inquilino
     */
    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        // The transaction may still be completing.
    }

    /**
     * Limpia el contexto del inquilino después de que se complete la
     * solicitud.
     * 
     * @param request la solicitud actual
     * @param ex      la excepción que se lanz&oacute; durante el manejo de la
     *                solicitud
     * @throws Exception si ocurre un error al limpiar el contexto del
     *                   inquilino
     */
    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
