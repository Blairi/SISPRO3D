package com.sispro3d.unam.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuración de los tokens JWT de la API.
 * La clave de firma y los tiempos de vida son externos al código y se
 * sobreescriben por entorno en cualquier despliegue real.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Clave simétrica de firma. Debe tener al menos 256 bits. */
    private String secret;

    /** Vigencia del token de acceso. */
    private Duration accessTtl = Duration.ofMinutes(30);

    /** Vigencia del token de refresco. */
    private Duration refreshTtl = Duration.ofDays(7);

    /** Identificador del emisor, viajando como claim {@code iss}. */
    private String issuer = "sispro3d";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTtl() {
        return accessTtl;
    }

    public void setAccessTtl(Duration accessTtl) {
        this.accessTtl = accessTtl;
    }

    public Duration getRefreshTtl() {
        return refreshTtl;
    }

    public void setRefreshTtl(Duration refreshTtl) {
        this.refreshTtl = refreshTtl;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}