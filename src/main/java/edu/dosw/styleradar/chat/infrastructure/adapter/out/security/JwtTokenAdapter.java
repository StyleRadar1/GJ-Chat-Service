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
        String subject = claims.getSubject();
        if (subject == null || !subject.matches("[1-9]\\d{0,18}")) {
            throw new JwtException("Subject JWT inválido");
        }
        return new AuthenticatedUser(Long.valueOf(subject), roles(claims.get("roles")));
    }

    private Set<String> roles(Object claim) {
        Set<String> roles = new LinkedHashSet<>();
        if (claim instanceof Collection<?> list) {
            list.stream().filter(String.class::isInstance).map(String.class::cast)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .filter(role -> !role.isBlank())
                    .forEach(roles::add);
        }
        if (roles.isEmpty()) {
            roles.add(defaultRole);
        }
        return Set.copyOf(roles);
    }
}
