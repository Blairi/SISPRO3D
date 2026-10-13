package com.sispro3d.unam.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tras iniciar sesión, el usuario regresa a la página que lo motivó a
 * autenticarse —por ejemplo el detalle de un servicio al pulsar «Contratar»—
 * y solo si no hay una petición guardada se envía a su panel según su rol.
 */
@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public RoleBasedAuthenticationSuccessHandler(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            redirectStrategy.sendRedirect(request, response, savedRequest.getRedirectUrl());
            return;
        }

        redirectStrategy.sendRedirect(request, response, homeFor(authentication));
    }

    private String homeFor(Authentication authentication) {
        var authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return "/admin/dashboard";
        }
        if (authorities.stream().anyMatch(a -> "ROLE_EXPERT".equals(a.getAuthority()))) {
            return "/expert/dashboard";
        }
        return "/client/quotes";
    }
}
