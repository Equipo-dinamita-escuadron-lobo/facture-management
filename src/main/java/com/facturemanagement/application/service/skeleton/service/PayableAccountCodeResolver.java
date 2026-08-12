package com.facturemanagement.application.service.skeleton.service;

import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Resuelve el código PUC auxiliar a partir del id de cuenta contable
 * consultando el catálogo. Confía en el {@code code} del catálogo aunque
 * coincida numéricamente con el id (códigos PUC enteramente numéricos).
 */
@Component
@Slf4j
public class PayableAccountCodeResolver {
    private final RestClient client;
    private final IJwtUtils jwtUtils;

    public PayableAccountCodeResolver(
            @Value("${baseUrl:http://localhost:8080}") String baseUrl,
            IJwtUtils jwtUtils) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.jwtUtils = jwtUtils;
    }

    public String resolveOrFail(Long accountId, String enterpriseId) {
        return resolve(accountId, enterpriseId).orElseThrow(() ->
                new IllegalStateException(
                        "No se pudo resolver el código PUC de la cuenta contable " + accountId
                                + " para la empresa " + enterpriseId));
    }

    public Optional<String> resolve(Long accountId, String enterpriseId) {
        if (accountId == null || enterpriseId == null || enterpriseId.isBlank()) {
            return Optional.empty();
        }
        try {
            AccountItem[] items = client.get()
                    .uri("/api/accountCatalogue/search/{enterpriseId}", enterpriseId)
                    .header("Authorization", "Bearer " + jwtUtils.getToken())
                    .header("X-Tenant-ID", Optional.ofNullable(TenantContext.getTenantId()).orElse(""))
                    .retrieve()
                    .body(AccountItem[].class);
            if (items == null) {
                return Optional.empty();
            }
            return Arrays.stream(items)
                    .filter(a -> accountId.equals(a.id()))
                    .map(AccountItem::code)
                    .filter(code -> code != null && !code.isBlank())
                    .findFirst();
        } catch (Exception ex) {
            log.warn("Lookup de cuenta {} en catálogo falló: {}", accountId, ex.getMessage());
            return Optional.empty();
        }
    }

    private record AccountItem(Long id, String code) {}
}
