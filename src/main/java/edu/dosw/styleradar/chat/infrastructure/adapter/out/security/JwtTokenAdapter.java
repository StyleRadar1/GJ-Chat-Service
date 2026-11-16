package edu.dosw.styleradar.chat.infrastructure.adapter.out.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Validates HS256 tokens and extracts the authenticated user. */
@Component
public class JwtTokenAdapter {
    private final JwtParser parser;
    private final Clock clock;
    private final String defaultRole;

    public JwtTokenAdapter(@Value("${styleradar.security.jwt.secret-base64:}") String secretBase64,
                           @Value("${styleradar.security.jwt.default-role:COMPRADOR}") String defaultRole,
                           Clock clock) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET_BASE64 debe contener una clave Base64 válida");
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET_BASE64 es requerida y debe contener al menos 256 bits");
        }
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.clock = clock;
        this.defaultRole = defaultRole;
        this.parser = Jwts.parser().verifyWith(key).clock(() -> Date.from(clock.instant())).build();
    }

    public AuthenticatedUser validate(String token) {
        Jws<Claims> jws = parser.parseSignedClaims(token);

        if (!Jwts.SIG.HS256.getId().equals(jws.getHeader().getAlgorithm())) {
            throw new JwtException("Algoritmo JWT no permitido");
        }
        Claims claims = jws.getPayload();
        Date now = Date.from(clock.instant());
        if (claims.getExpiration() == null || claims.getIssuedAt() == null
                || !claims.getExpiration().after(now) || claims.getIssuedAt().after(now)) {
            throw new JwtException("Fechas JWT inválidas");
        }
        return new AuthenticatedUser(userId(claims), roles(claims));
    }

    private Long userId(Claims claims) {
        Object idClaim = claims.get("userId");
        String candidate = idClaim == null ? claims.getSubject() : idClaim.toString();
        if (candidate == null || !candidate.matches("[1-9]\\d{0,18}")) {
            throw new JwtException("El JWT debe incluir userId numérico o un sub numérico heredado");
        }
        try {
            return Long.valueOf(candidate);
        } catch (NumberFormatException exception) {
            throw new JwtException("userId JWT inválido", exception);
        }
    }

    private Set<String> roles(Claims claims) {
        Set<String> roles = new LinkedHashSet<>();
        addRoles(roles, claims.get("roles"));
        addRoles(roles, claims.get("role"));
        if (roles.isEmpty()) {
            roles.add(defaultRole);
        }
        return Set.copyOf(roles);
    }

    private void addRoles(Set<String> roles, Object claim) {
        if (claim instanceof Collection<?> values) {
            values.stream().filter(String.class::isInstance).map(String.class::cast)
                    .map(this::toChatRole).filter(role -> !role.isBlank()).forEach(roles::add);
        } else if (claim instanceof String role) {
            String normalized = toChatRole(role);
            if (!normalized.isBlank()) {
                roles.add(normalized);
            }
        }
    }

    private String toChatRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        // The shared identity model calls ordinary marketplace users USER; ms-chat calls them COMPRADOR.
        return normalized.equals("USER") ? "COMPRADOR" : normalized;
    }
}
