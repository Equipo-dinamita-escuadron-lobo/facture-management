package com.facturemanagement.infraestructure.adapters.input.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.net.URI;

import org.apache.catalina.connector.Response;
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
    public String testAll() {
        return "pong";
    }

    @GetMapping("/testExternal")
    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    public String getEnterprises(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer "+ jwtUtils.getToken());
        HttpEntity<String> entity = new HttpEntity<>( headers);
        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("http://contables.unicauca.edu.co/api/enterprises/"),
            HttpMethod.GET,
            entity,
            String.class
        );

        return response.getBody();
    }

    public String fallback(Throwable t){
        return "Fallback: "+t.getMessage();
    }

}
