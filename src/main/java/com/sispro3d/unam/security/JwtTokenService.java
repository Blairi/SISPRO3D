package com.sispro3d.unam.security;

import com.sispro3d.unam.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Emite y valida los tokens firmados de la API. El payload solo lleva datos
 * de identidad —identificador de cuenta y rol— nunca material de contraseña.
 */
@Service
public class JwtTokenService {

    static final String CLAIM_ROLE = "role";
    static final String CLAIM_TOKEN_TYPE = "typ";
    static final String CLAIM_TOKEN_ID = "jti";
    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(Long accountId, Role role) {
        return build(accountId, role, TYPE_ACCESS, properties.getAccessTtl());
    }

    public String issueRefreshToken(Long accountId, Role role) {
        return build(accountId, role, TYPE_REFRESH, properties.getRefreshTtl());
    }

    private String build(Long accountId, Role role, String type, java.time.Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(accountId))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TOKEN_TYPE, type)
                .claim(CLAIM_TOKEN_ID, java.util.UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifica la firma y la vigencia del token. Devuelve vacío si el token es
     * ilegible, está vencido o no es del tipo esperado.
     */
    public Optional<Claims> parse(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<JwtPrincipal> parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS)
                .map(this::toPrincipal);
    }

    private JwtPrincipal toPrincipal(Claims claims) {
        Long accountId = Long.valueOf(claims.getSubject());
        Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
        return new JwtPrincipal(accountId, role, claims.getId());
    }

    /** Identidad verificada extraída de un token de acceso. */
    public record JwtPrincipal(Long accountId, Role role, String tokenId) {
    }

    /** Credenciales emitidas junto con un token de acceso. */
    public record IssuedTokens(String accessToken, String refreshToken, long expiresInSeconds, Map<String, String> claims) {
    }
}
