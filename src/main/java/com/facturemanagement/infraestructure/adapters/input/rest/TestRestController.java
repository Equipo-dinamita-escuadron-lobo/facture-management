package com.facturemanagement.infraestructure.adapters.input.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.IOException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

@RestController
@RequestMapping("/api/factures/test")
public class TestRestController {

    @Autowired
    private IJwtUtils jwtUtils;

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Prueba básica accesible para cualquier usuario para verificar la disponibilidad del servidor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servidor responde correctamente", content = @Content(mediaType = "text/plain"))
    })
    public String testAll() {
        return "pong";
    }

    @GetMapping("/testExternal")
    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @Operation(summary = "Prueba de servicio externo", description = "Realiza una llamada a un servicio externo para obtener información de empresas. Usa un Circuit Breaker para manejar fallas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta exitosa del servicio externo", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno o fallo en el servicio externo", content = @Content)
    })
    public JsonNode getEnterprises() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtils.getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                URI.create("http://contables.unicauca.edu.co/api/thirds/third?thId=1"),
                HttpMethod.GET,
                entity,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Leer el JSON como un objeto JsonNode
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // Manipulación adicional del JsonNode si es necesario
            return jsonNode;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método de fallback para manejar fallas en el servicio externo.
     *
     * @param t Excepción capturada.
     * @return Respuesta de fallback con un mensaje de error.
     */
    public String fallback(Throwable t) {
        return "Fallback: " + t.getMessage();
    }

}
