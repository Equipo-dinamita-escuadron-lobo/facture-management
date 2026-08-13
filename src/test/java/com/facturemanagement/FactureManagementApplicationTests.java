package com.facturemanagement;

import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = FactureManagementApplicationTests.TenantInitializer.class)
class FactureManagementApplicationTests {
	static class TenantInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override public void initialize(ConfigurableApplicationContext context) {
			TenantContext.setTenantId("test-tenant");
		}
	}

	@AfterAll
	static void clearTenant() { TenantContext.clear(); }

	@Test
	void contextLoads() {
	}

}
