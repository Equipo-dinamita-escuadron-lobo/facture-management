package com.facturemanagement.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PayableAccountDefaultResolverUnitTest {

    private HttpServer server;
    private String baseUrl;
    private PayableAccountDefaultResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        IJwtUtils jwtUtils = Mockito.mock(IJwtUtils.class);
        Mockito.when(jwtUtils.getToken()).thenReturn("token");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        resolver = new PayableAccountDefaultResolver(baseUrl, jwtUtils, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void resolvesDefaultSupplierPayableAccountFromCatalogue() {
        stubCatalogue("""
                [
                  {"id":10,"code":"1105","description":"Caja","classification":"Activo Corriente","status":true},
                  {"id":24,"code":"2206","description":"Beneficios a empleados a largo plazo","classification":"Pasivo No Corriente","status":true},
                  {"id":21,"code":"2105","description":"Cuentas por pagar","classification":"Pasivo Corriente","status":true}
                ]
                """);

        assertThat(resolver.resolveForPurchase(null, "enterprise-a")).isEqualTo(21L);
    }

    @Test
    void acceptsAccountsPayableDescription() {
        stubCatalogue("""
                [{"id":21,"code":"2105","description":"Cuentas por pagar","classification":"Pasivo Corriente","status":true}]
                """);

        assertThat(resolver.resolveForPurchase(null, "enterprise-a")).isEqualTo(21L);
    }

    @Test
    void acceptsSuppliersDescription() {
        stubCatalogue("""
                [{"id":30,"code":"220501","description":"Proveedores nacionales","classification":"Pasivo Corriente","status":true}]
                """);

        assertThat(resolver.resolveForPurchase(null, "enterprise-a")).isEqualTo(30L);
    }

    @Test
    void rejectsCashAccount() {
        stubCatalogue("""
                [{"id":10,"code":"1105","description":"Caja","classification":"Activo Corriente","status":true}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsEmployeeBenefitsAccount() {
        stubCatalogue("""
                [{"id":24,"code":"2206","description":"Beneficios a empleados a largo plazo","classification":"Pasivo No Corriente","status":true}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsLaborObligationsAccount() {
        stubCatalogue("""
                [{"id":31,"code":"2510","description":"Obligaciones laborales","classification":"Pasivo Corriente","status":true}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsInactiveExplicitPayableAccount() {
        stubCatalogue("""
                [{"id":55,"code":"2105","description":"Cuentas por pagar","classification":"Pasivo Corriente","status":false}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(55L, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void rejectsWhenCatalogueIsEmpty() {
        stubCatalogue("[]");

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catálogo de cuentas válido");
    }

    @Test
    void rejectsWhenNoSupplierPayableExists() {
        stubCatalogue("""
                [
                  {"id":1,"code":"1105","description":"Caja","classification":"Activo Corriente","status":true},
                  {"id":2,"code":"2206","description":"Beneficios a empleados a largo plazo","classification":"Pasivo No Corriente","status":true},
                  {"id":3,"code":"2205","description":"Obligaciones financieras no corrientes","classification":"Pasivo No Corriente","status":true}
                ]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void acceptsExplicitActiveSupplierPayableAccount() {
        stubCatalogue("""
                [{"id":77,"code":"2105","description":"Cuentas por pagar","classification":"Pasivo Corriente","status":true}]
                """);

        assertThat(resolver.resolveForPurchase(77L, "enterprise-a")).isEqualTo(77L);
    }

    @Test
    void rejectsExplicitNonSupplierPayableAccount() {
        stubCatalogue("""
                [{"id":24,"code":"2206","description":"Beneficios a empleados a largo plazo","classification":"Pasivo No Corriente","status":true}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(24L, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es una cuenta por pagar válida");
    }

    @Test
    void prefersAccountsPayableOverSuppliersWhenBothExist() {
        stubCatalogue("""
                [
                  {"id":30,"code":"220501","description":"Proveedores","classification":"Pasivo Corriente","status":true},
                  {"id":21,"code":"2105","description":"Cuentas por pagar","classification":"Pasivo Corriente","status":true}
                ]
                """);

        assertThat(resolver.resolveForPurchase(null, "enterprise-a")).isEqualTo(21L);
    }

    private void stubCatalogue(String jsonBody) {
        server.createContext("/api/accountCatalogue/search/enterprise-a", exchange -> {
            byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
    }
}
