package com.sispro3d.unam.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityAccount securityAccount)) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return securityAccount.getId();
    }
}