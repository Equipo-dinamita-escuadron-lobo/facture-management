package com.facturemanagement.infraestructure.adapters.security;

import java.util.List;

public interface IJwtUtils {
    String getId();

    String getToken();

    String getUsername();

    List<String> getRealmRoles();
}
