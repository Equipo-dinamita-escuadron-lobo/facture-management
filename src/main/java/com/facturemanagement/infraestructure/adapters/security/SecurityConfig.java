package com.facturemanagement.infraestructure.adapters.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    /**
     * Este bean es responsable de configurar la cadena de filtros de Spring
     * Security.
     * La cadena de filtros se configura para deshabilitar csrf, permitir que
     * swagger-ui y v3/api-docs se accedan sin autenticación,
     * y requerir autenticación para todas las demás solicitudes. La autenticación
     * se realiza utilizando JWT.
     * La política de creación de sesión se establece en STATELESS, lo que significa
     * que no se creará ninguna sesión.
     * 
     * @return una SecurityFilterChain que se utilizará para proteger la aplicación
     * @throws Exception si algo sale mal mientras se construye la cadena de filtros
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
