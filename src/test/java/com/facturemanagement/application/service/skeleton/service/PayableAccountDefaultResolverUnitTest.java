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
    void resolvesDefaultActivePayableAccountFromCatalogue() {
        stubCatalogue("""
                [
                  {"id":10,"code":"1105","classification":"Activo Corriente","status":true},
                  {"id":99,"code":"22050101","classification":"Pasivo Corriente","status":true},
                  {"id":98,"code":"2205","classification":"Pasivo No Corriente","status":true}
                ]
                """);

        assertThat(resolver.resolveForPurchase(null, "enterprise-a")).isEqualTo(99L);
    }

    @Test
    void rejectsInactiveExplicitPayableAccount() {
        stubCatalogue("""
                [{"id":55,"code":"22050101","classification":"Pasivo Corriente","status":false}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(55L, "enterprise-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void rejectsWhenNoActivePayableExists() {
        stubCatalogue("""
                [{"id":1,"code":"1105","classification":"Activo Corriente","status":true}]
                """);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "enterprise-a"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No hay cuentas por pagar activas");
    }

    @Test
    void acceptsExplicitActivePayableAccount() {
        stubCatalogue("""
                [{"id":77,"code":"22050101","classification":"Pasivo Corriente","status":true}]
                """);

        assertThat(resolver.resolveForPurchase(77L, "enterprise-a")).isEqualTo(77L);
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
