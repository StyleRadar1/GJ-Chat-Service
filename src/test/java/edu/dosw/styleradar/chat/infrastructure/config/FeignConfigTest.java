package edu.dosw.styleradar.chat.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTest {
    private final FeignConfig config = new FeignConfig();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsAuthorizationHeaderWhenPresent() {
        RequestInterceptor interceptor = config.forwardAuthorization();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get("Authorization")).containsExactly("Bearer test-token");
    }

    @Test
    void doesNothingWhenAuthorizationHeaderIsMissing() {
        RequestInterceptor interceptor = config.forwardAuthorization();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().containsKey("Authorization")).isFalse();
    }

    @Test
    void doesNothingWhenRequestContextIsNull() {
        RequestInterceptor interceptor = config.forwardAuthorization();
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().containsKey("Authorization")).isFalse();
    }
}
