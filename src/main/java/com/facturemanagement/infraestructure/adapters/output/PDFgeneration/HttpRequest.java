package com.facturemanagement.infraestructure.adapters.output.PDFgeneration;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpRequest {

    /**
     * Realiza una solicitud GET a la URL dada con el token de autenticaci n
     * proporcionado por jwtUtils.
     *
     * @param url La URL a la que se va a realizar la solicitud.
     * @param jwtUtils El objeto que proporciona el token de autenticaci n.
     * @return El contenido de la respuesta como un objeto JsonNode. Si ocurre un
     *         error al leer el contenido de la respuesta, se devuelve null.
     */
    public JsonNode getRequest(String url,IJwtUtils jwtUtils){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer "+ jwtUtils.getToken());
        HttpEntity<String> entity = new HttpEntity<>( headers);
        ResponseEntity<String> response = restTemplate.exchange(
            URI.create(url),
            HttpMethod.GET,
            entity,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Leer el JSON como un objeto JsonNode
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // Puedes manipular el JsonNode según tus necesidades
            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
