package com.example.CRUD.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long id() {
        return Long.valueOf(jwt().getSubject());
    }

    public String role() {
        return jwt().getClaimAsString("role");
    }

    private Jwt jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new AccessDeniedException("Not authenticated");
    }
}