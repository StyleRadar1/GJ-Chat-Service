package edu.dosw.styleradar.chat.support;

import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/** Builds signed JWTs for the tests. */
public final class Tokens {
    public static final String SECRET = "c3R5bGVyYWRhci1tcy1jaGF0LWxsYXZlLXNvbG8tcGFyYS1wcnVlYmFzISE=";

    private Tokens() {
    }

    public static String forUser(Long userId) {
        return sign(Jwts.builder().subject(userId.toString()), Instant.now(), SECRET);
    }

    public static String withRoles(Long userId, List<String> roles) {
        return sign(Jwts.builder().subject(userId.toString()).claim("roles", roles), Instant.now(), SECRET);
    }

    public static String expired(Long userId) {
        return sign(Jwts.builder().subject(userId.toString()), Instant.now().minusSeconds(7200), SECRET);
    }

    public static String withAnotherKey(Long userId) {
        String other = Base64.getEncoder().encodeToString("another-key-distinct-de-al-menos-32-bytes!!".getBytes());
        return sign(Jwts.builder().subject(userId.toString()), Instant.now(), other);
    }

    public static String withSubject(String subject) {
        return sign(Jwts.builder().subject(subject), Instant.now(), SECRET);
    }

    public static String withHs512Algorithm(Long userId) {
        var key = new SecretKeySpec(new byte[64], "HmacSHA512");
        return Jwts.builder().subject(userId.toString()).issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600))).signWith(key, Jwts.SIG.HS512).compact();
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String sign(io.jsonwebtoken.JwtBuilder builder, Instant issued, String secret) {
        var key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        return builder.issuedAt(Date.from(issued)).expiration(Date.from(issued.plusSeconds(3600)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }
}
