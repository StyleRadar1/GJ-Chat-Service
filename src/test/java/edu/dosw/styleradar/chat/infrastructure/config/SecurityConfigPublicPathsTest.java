package edu.dosw.styleradar.chat.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Las métricas del servicio no deben ser públicas; salud y documentación sí. */
class SecurityConfigPublicPathsTest {
    @Test
    void metricsAndPrometheusRequireAToken() {
        assertThat(SecurityConfig.PUBLIC_PATHS)
                .doesNotContain("/actuator/metrics/**", "/actuator/prometheus", "/actuator/metrics");
    }

    @Test
    void healthAndDocumentationStayPublic() {
        assertThat(SecurityConfig.PUBLIC_PATHS)
                .contains("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }
}
