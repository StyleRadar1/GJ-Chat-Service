package edu.dosw.styleradar.chat.infrastructure.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/** Custom Feign configuration for internal Users Directory calls requiring Service Basic Auth. */
public class UsersDirectoryFeignConfig {
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${styleradar.services.users.username:service-chat}") String username,
            @Value("${styleradar.services.users.password:service-secret}") String password) {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
