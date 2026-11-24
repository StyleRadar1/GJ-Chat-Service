package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void disablesDefaultFilterRegistration() {
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);
        FilterRegistrationBean<JwtAuthenticationFilter> registration = config.jwtFilterRegistration(filter);

        assertThat(registration).isNotNull();
        assertThat(registration.isEnabled()).isFalse();
    }
}
