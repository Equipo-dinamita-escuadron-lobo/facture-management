package com.facturemanagement.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * L-03: lab supplier account often has id == PUC code (e.g. 2205 / "2205").
 * The resolver must accept that numeric PUC code after a successful catalogue lookup.
 */
class PayableAccountCodeResolverTest {

    private HttpServer server;
    private String baseUrl;
    private IJwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws IOException {
        jwtUtils = mock(IJwtUtils.class);
        when(jwtUtils.getToken()).thenReturn("test-token");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void resolveAcceptsNumericPucCodeWhenItEqualsAccountId() {
        stubCatalogueSearch(
                "enterprise-local",
                "[{\"id\":2205,\"code\":\"2205\"}]");

        PayableAccountCodeResolver resolver = new PayableAccountCodeResolver(baseUrl, jwtUtils);

        Optional<String> code = resolver.resolve(2205L, "enterprise-local");

        assertThat(code).contains("2205");
    }

    @Test
    void resolveStillReturnsDistinctCodeWhenIdAndCodeDiffer() {
        stubCatalogueSearch(
                "enterprise-local",
                "[{\"id\":99,\"code\":\"2205\"}]");

        PayableAccountCodeResolver resolver = new PayableAccountCodeResolver(baseUrl, jwtUtils);

        assertThat(resolver.resolve(99L, "enterprise-local")).contains("2205");
    }

    private void stubCatalogueSearch(String enterpriseId, String jsonBody) {
        server.createContext("/api/accountCatalogue/search/" + enterpriseId, exchange -> {
            byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
    }
}
