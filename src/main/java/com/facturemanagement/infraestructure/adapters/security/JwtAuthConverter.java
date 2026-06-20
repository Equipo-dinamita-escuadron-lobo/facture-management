package com.facturemanagement.infraestructure.adapters.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken>, IJwtUtils {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAtrribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    Jwt jwtToken;

    /**
     * Convierte un objeto Jwt en un AbstractAuthenticationToken.
     * Este método extrae las autorizaciones concedidas del token JWT
     * y las combina con roles de recursos para construir el token de autenticación.
     * También establece el token JWT para su uso posterior.
     *
     * @param jwt el objeto Jwt a convertir
     * @return un JwtAuthenticationToken que contiene el token y las autorizaciones
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), extractResourceRoles(jwt).stream())
                .toList();

        this.jwtToken = jwt;

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleName(jwt));

    }

    /**
     * Extrae el valor del campo de la JWT que se usar como el nombre del usuario.
     * Por defecto se utiliza el campo "sub", pero se puede sobreescribir
     * especificando el nombre del campo en la propiedad
     * "jwt.auth.converter.principle-attribute".
     * 
     * @param jwt el objeto Jwt del que se extraer el nombre del usuario
     * @return el nombre del usuario
     */
    private String getPrincipleName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;

        if (principleAtrribute != null) {
            claimName = principleAtrribute;
        }

        return jwt.getClaim(claimName);
    }

    /**
     * Extrae las roles de un recurso de la JWT.
     * Primero, busca el campo "resource_access" en la JWT.
     * Luego, busca el campo del recurso especificado en la propiedad
     * "jwt.auth.converter.resource-id".
     * A continuaci n, busca el campo "roles" en el recurso.
     * Finalmente, devuelve una lista de GrantedAuthority que contienen las roles
     * del recurso
     * con el prefijo "ROLE_". Si no se encuentra el campo "resource_access" o el
     * campo del recurso,
     * se devuelve una lista vac a.
     * 
     * @param jwt el objeto Jwt del que se extraen las roles del recurso
     * @return una lista de GrantedAuthority que contienen las roles del recurso
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (jwt.getClaim("resource_access") == null) {
            return List.of();
        }

        resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess.get(resourceId) == null) {
            return List.of();
        }

        resource = (Map<String, Object>) resourceAccess.get(resourceId);

        if (resource.get("roles") == null) {
            return List.of();
        }

        resourceRoles = (Collection<String>) resource.get("roles");

        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role)))
                .toList();
    }

    /**
     * Recupera el identificador de usuario del token JWT.
     * Este método extrae el valor del reclamo "sub" de las reivindicaciones JWT,
     * que generalmente se utiliza como identificador único del usuario.
     *
     * @return el identificador de usuario como una cadena de texto
     */
    @Override
    public String getId() {
        return (String) jwtToken.getClaims().get("sub");
    }

    /**
     * Recupera el token JWT como una cadena de texto.
     * 
     * @return el token JWT como una cadena de texto
     */
    @Override
    public String getToken() {
        return jwtToken.getTokenValue();
    }

    @Override
    public String getUsername() {
        return (String) jwtToken.getClaims().get("preferred_username");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getRealmRoles() {
        Map<String, Object> realmAccess = (Map<String, Object>) jwtToken.getClaims().get("realm_access");

        if (realmAccess == null)
            return List.of();

        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof List<?> rolesList))
            return List.of();

        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
