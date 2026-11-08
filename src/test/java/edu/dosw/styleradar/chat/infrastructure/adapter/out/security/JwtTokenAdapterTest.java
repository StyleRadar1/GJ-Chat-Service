package edu.dosw.styleradar.chat.infrastructure.adapter.out.security;

import edu.dosw.styleradar.chat.support.Tokens;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for JWT validation. */
class JwtTokenAdapterTest {
    private final JwtTokenAdapter adapter = new JwtTokenAdapter(Tokens.SECRET, "COMPRADOR", Clock.systemUTC());

    @Test
    void withoutRolesClaimAssignsDefaultRole() {
        AuthenticatedUser user = adapter.validate(Tokens.forUser(5L));

        assertThat(user.userId()).isEqualTo(5L);
        assertThat(user.roles()).containsExactly("COMPRADOR");
        assertThat(user.authorities()).extracting("authority").containsExactly("ROLE_COMPRADOR");
    }

    @Test
    void usesTokenRolesWithOrWithoutPrefix() {
        AuthenticatedUser user = adapter.validate(Tokens.withRoles(5L, List.of("ROLE_ADMIN_ALMACEN", "COMPRADOR", " ")));

        assertThat(user.roles()).containsExactlyInAnyOrder("ADMIN_ALMACEN", "COMPRADOR");
    }

    @Test
    void rejectsExpiredToken() {
        assertThatThrownBy(() -> adapter.validate(Tokens.expired(5L))).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        assertThatThrownBy(() -> adapter.validate(Tokens.withAnotherKey(5L))).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsAlgorithmOtherThanHs256() {
        String secret512 = java.util.Base64.getEncoder().encodeToString(new byte[64]);
        JwtTokenAdapter adapter512 = new JwtTokenAdapter(secret512, "COMPRADOR", Clock.systemUTC());
        assertThatThrownBy(() -> adapter512.validate(Tokens.withHs512Algorithm(5L))).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> adapter.validate("no-es-un-jwt")).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsNonNumericSubject() {
        assertThatThrownBy(() -> adapter.validate(Tokens.withSubject("abc"))).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> adapter.validate(Tokens.withSubject("0"))).isInstanceOf(JwtException.class);
    }

    @Test
    void requiresAKeyOfAtLeast256Bits() {
        assertThatThrownBy(() -> new JwtTokenAdapter("", "COMPRADOR", Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("256");
        assertThatThrownBy(() -> new JwtTokenAdapter("not-base64!", "COMPRADOR", Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Base64");
    }
}
