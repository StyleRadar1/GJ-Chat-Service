package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

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

    @Test
    void configuresCorsWithAllowedOrigins() {
        CorsConfigurationSource source = config.corsConfigurationSource("http://localhost:3000, https://styleradar.com ");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/conversaciones");

        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).containsExactly("http://localhost:3000", "https://styleradar.com");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowedHeaders()).contains("Authorization", "Content-Type");
    }

    @Test
    void configuresCorsWithEmptyOrigins() {
        CorsConfigurationSource source = config.corsConfigurationSource("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).isEmpty();
    }
}
