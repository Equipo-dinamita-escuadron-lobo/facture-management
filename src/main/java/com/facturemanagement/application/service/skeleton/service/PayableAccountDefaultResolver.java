package com.facturemanagement.application.service.skeleton.service;

import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Resuelve la cuenta CxP para facturas de compra consultando el catálogo real.
 * Alineado con la regla de Tesorería: pasivo corriente o código PUC 22xxxx.
 */
@Component
@Slf4j
public class PayableAccountDefaultResolver {

    static final String NO_VALID_CATALOGUE_MESSAGE =
            "La empresa aún no tiene un catálogo de cuentas válido. Configure el catálogo de cuentas "
                    + "en Maestros Generales antes de registrar facturas de compra.";
    static final String NO_ACTIVE_PAYABLE_MESSAGE =
            "El catálogo de la empresa no tiene cuentas por pagar activas. Cree o active una cuenta CxP "
                    + "(por ejemplo código 22xxxx) en el catálogo de cuentas.";

    private final RestClient client;
    private final IJwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public PayableAccountDefaultResolver(
            @Value("${baseUrl:http://localhost:8080}") String baseUrl,
            IJwtUtils jwtUtils,
            ObjectMapper objectMapper) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * @param requestedAccountId cuenta explícita (API legacy); null para default automático
     */
    public Long resolveForPurchase(Long requestedAccountId, String enterpriseId) {
        List<CatalogueAccount> accounts = loadAccounts(enterpriseId);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException(NO_VALID_CATALOGUE_MESSAGE);
        }
        if (requestedAccountId != null) {
            CatalogueAccount explicit = accounts.stream()
                    .filter(account -> requestedAccountId.equals(account.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La cuenta contable " + requestedAccountId + " no existe en el catálogo de la empresa"));
            if (!isActive(explicit)) {
                throw new IllegalArgumentException(
                        "La cuenta CxP " + requestedAccountId + " está inactiva y no puede usarse en facturas nuevas");
            }
            if (!isPayableAccount(explicit)) {
                throw new IllegalArgumentException(
                        "La cuenta " + requestedAccountId + " no es una cuenta por pagar válida");
            }
            return explicit.id();
        }
        return selectDefaultPayable(accounts)
                .map(CatalogueAccount::id)
                .orElseThrow(() -> new IllegalArgumentException(NO_ACTIVE_PAYABLE_MESSAGE));
    }

    private Optional<CatalogueAccount> selectDefaultPayable(List<CatalogueAccount> accounts) {
        return accounts.stream()
                .filter(account -> isActive(account) && isPayableAccount(account))
                .sorted(Comparator
                        .comparingInt((CatalogueAccount account) -> account.code() == null ? 0 : account.code().length())
                        .reversed()
                        .thenComparing(CatalogueAccount::code, Comparator.nullsLast(String::compareTo)))
                .findFirst();
    }

    /** Misma regla que expense-receipt-creation en ContAppAng19. */
    boolean isPayableAccount(CatalogueAccount account) {
        String code = account.code() == null ? "" : account.code();
        String classification = account.classification() == null
                ? ""
                : account.classification().toLowerCase(Locale.ROOT);
        String description = account.description() == null
                ? ""
                : account.description().toLowerCase(Locale.ROOT);
        if (code.startsWith("22")) {
            return true;
        }
        return classification.contains("pasivo corriente")
                || description.contains("cuentas por pagar")
                || description.contains("cuenta por pagar")
                || description.contains("proveedores");
    }

    private boolean isActive(CatalogueAccount account) {
        return account.status() == null || Boolean.TRUE.equals(account.status());
    }

    private List<CatalogueAccount> loadAccounts(String enterpriseId) {
        try {
            String body = client.get()
                    .uri("/api/accountCatalogue/search/{enterpriseId}", enterpriseId)
                    .header("Authorization", "Bearer " + jwtUtils.getToken())
                    .header("X-Tenant-ID", Optional.ofNullable(TenantContext.getTenantId()).orElse(""))
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(body);
            List<CatalogueAccount> accounts = new ArrayList<>();
            for (JsonNode node : root) {
                accounts.add(new CatalogueAccount(
                        node.path("id").asLong(),
                        textOrNull(node, "code"),
                        textOrNull(node, "description"),
                        textOrNull(node, "classification"),
                        node.has("status") && !node.get("status").isNull() ? node.get("status").asBoolean() : null));
            }
            return accounts;
        } catch (Exception ex) {
            log.warn("No se pudo consultar catálogo para empresa {}: {}", enterpriseId, ex.getMessage());
            throw new IllegalStateException("No se pudo consultar el catálogo de cuentas para resolver CxP", ex);
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    record CatalogueAccount(Long id, String code, String description, String classification, Boolean status) {}
}
