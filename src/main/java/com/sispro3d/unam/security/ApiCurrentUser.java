package com.sispro3d.unam.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Identidad de la llamada API en curso, resuelta desde el token verificado.
 * Sustituye a los identificadores de actor que antes viajaban en el cuerpo
 * de las peticiones.
 */
public final class ApiCurrentUser {

    private ApiCurrentUser() {
    }

    public static Optional<JwtTokenService.JwtPrincipal> principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof JwtTokenService.JwtPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static JwtTokenService.JwtPrincipal require() {
        return principal().orElseThrow(() -> new IllegalStateException("Llamada sin token de acceso válido"));
    }

    public static Long requireAccountId() {
        return require().accountId();
    }
}
