package com.facturemanagement.infraestructure.adapters.security;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class FactureServiceTokenProvider {
    private final RestClient restClient=RestClient.create();private final String uri;private final String clientId;private final String secret;private volatile CachedToken cached;
    public FactureServiceTokenProvider(@Value("${facture.security.service-token.uri}")String uri,@Value("${facture.security.service-token.client-id}")String clientId,@Value("${facture.security.service-token.client-secret:}")String secret){this.uri=uri;this.clientId=clientId;this.secret=secret;}
    public synchronized String bearerToken(){Instant now=Instant.now();if(cached!=null&&cached.expiresAt().isAfter(now.plusSeconds(30)))return cached.value();if(secret==null||secret.isBlank())throw new IllegalStateException("FACTURE_OAUTH_CLIENT_SECRET no esta configurado");var form=new LinkedMultiValueMap<String,String>();form.add("grant_type","client_credentials");form.add("client_id",clientId);form.add("client_secret",secret);TokenResponse response=restClient.post().uri(uri).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(TokenResponse.class);if(response==null||response.access_token()==null||response.access_token().isBlank())throw new IllegalStateException("Keycloak no devolvio token de facture-service");cached=new CachedToken("Bearer "+response.access_token(),now.plusSeconds(Math.max(1,response.expires_in())));return cached.value();}
    private record TokenResponse(String access_token,long expires_in){}private record CachedToken(String value,Instant expiresAt){}
}
