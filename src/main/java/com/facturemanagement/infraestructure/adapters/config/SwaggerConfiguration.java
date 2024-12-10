package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfiguration {
        /**
         * Personaliza la configuración de OpenAPI de la API.
         *
         * Agrega un requisito de seguridad de tipo Bearer JWT HTTP y establece el
         * título, descripción y
         * versión de la API.
         *
         * @return la configuración personalizada de OpenAPI
         */
        @Bean
        public OpenAPI customizeOpenAPI() {
                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                                .info(new Info().title("Facture Management API")
                                                .description("Api para gestión de facturas")
                                                .version("1.0"));
        }
}
