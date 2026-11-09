package edu.dosw.styleradar.chat.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Enables Feign clients and forwards the caller Authorization header to downstream services. */
@Configuration
@EnableFeignClients(basePackages = "edu.dosw.styleradar.chat.infrastructure.adapter.out.external")
public class FeignConfig {
    @Bean
    public RequestInterceptor forwardAuthorization() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                String authorization = attributes.getRequest().getHeader("Authorization");
                if (authorization != null) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
