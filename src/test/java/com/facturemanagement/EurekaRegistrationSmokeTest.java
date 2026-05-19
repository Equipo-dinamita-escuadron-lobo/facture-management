package com.facturemanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifica que el contexto Spring arranca con Eureka deshabilitado (CI-safe).
 * REQ-FACTURES-01, ADR-37.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:facture_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/dummy",
    "spring.cloud.discovery.enabled=false"
})
@DisplayName("Eureka registration smoke test (contexto arranca sin error)")
class EurekaRegistrationSmokeTest {

    @Test
    @DisplayName("ApplicationContext carga correctamente con Eureka deshabilitado")
    void contextLoads() {
        // Solo verifica que el contexto arranca sin lanzar excepciones
    }
}
