package edu.dosw.styleradar.chat.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger metadata and Bearer security scheme. */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI chatOpenApi() {
        return new OpenAPI()
                .info(new Info().title("StyleRadar · Chat Service")
                        .description("Private messaging service for second-hand items, partner stores, and donations")
                        .version("0.1.0"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
