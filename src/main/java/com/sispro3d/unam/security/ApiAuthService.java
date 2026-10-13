package com.sispro3d.unam.security;

import com.sispro3d.unam.api.dto.TokenResponseDTO;
import com.sispro3d.unam.revokedtoken.domain.RevokedToken;
import com.sispro3d.unam.revokedtoken.repository.RevokedTokenRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Autenticación por token de la API: canje de credenciales por un par
 * acceso/refresco, renovación del token de acceso y revocación al cerrar sesión.
 */
@Service
public class ApiAuthService {

    private final AccountRepository accountRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final JwtProperties properties;

    public ApiAuthService(AccountRepository accountRepository,
                          RevokedTokenRepository revokedTokenRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenService tokenService,
                          JwtProperties properties) {
        this.accountRepository = accountRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    /**
     * Un correo desconocido y una contraseña incorrecta producen el mismo error
     * para no revelar qué cuentas existen.
     */
    @Transactional
    public TokenResponseDTO authenticate(String email, String rawPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(rawPassword, account.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return issuePair(account);
    }

    @Transactional
    public TokenResponseDTO refresh(String refreshToken) {
        Claims claims = tokenService.parse(refreshToken, JwtTokenService.TYPE_REFRESH)
                .orElseThrow(() -> new BadCredentialsException("Token de refresco inválido"));

        String tokenId = claims.getId();
        if (tokenId != null && revokedTokenRepository.existsById(tokenId)) {
            throw new BadCredentialsException("Token de refresco revocado");
        }

        Long accountId = Long.valueOf(claims.getSubject());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadCredentialsException("Token de refresco inválido"));

        return issuePair(account);
    }

    /** Revoca el token de refresco presentado, cerrando la sesión del cliente. */
    @Transactional
    public void logout(String refreshToken) {
        tokenService.parse(refreshToken, JwtTokenService.TYPE_REFRESH)
                .map(Claims::getId)
                .filter(id -> !revokedTokenRepository.existsById(id))
                .ifPresent(id -> revokedTokenRepository.save(new RevokedToken(id)));
    }

    private TokenResponseDTO issuePair(Account account) {
        Role role = account.getRole();
        String accessToken = tokenService.issueAccessToken(account.getIdUser(), role);
        String refreshToken = tokenService.issueRefreshToken(account.getIdUser(), role);

        Map<String, String> claims = new LinkedHashMap<>();
        claims.put(JwtTokenService.CLAIM_ROLE, role.name());
        claims.put("sub", String.valueOf(account.getIdUser()));
        claims.put("iss", properties.getIssuer());

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(properties.getAccessTtl().toSeconds())
                .claims(claims)
                .build();
    }

    public Optional<JwtTokenService.JwtPrincipal> principalFrom(String accessToken) {
        return tokenService.parseAccessToken(accessToken);
    }
}
