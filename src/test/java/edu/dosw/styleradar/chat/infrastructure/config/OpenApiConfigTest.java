package edu.dosw.styleradar.chat.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {
    @Test
    void configuresOpenApiMetadata() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.chatOpenApi();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).contains("StyleRadar");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
    }
}
