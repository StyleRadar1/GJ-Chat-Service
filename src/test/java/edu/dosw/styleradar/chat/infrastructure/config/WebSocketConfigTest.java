package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket.JwtStompChannelInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {
    @Mock
    private JwtStompChannelInterceptor jwtInterceptor;

    @Test
    void configuresWebSocketMessageBroker() {
        WebSocketConfig config = new WebSocketConfig(jwtInterceptor, "http://localhost:3000, https://styleradar.com ");

        MessageBrokerRegistry brokerRegistry = mock(MessageBrokerRegistry.class);
        config.configureMessageBroker(brokerRegistry);
        verify(brokerRegistry).enableSimpleBroker("/topic");
        verify(brokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(brokerRegistry).setUserDestinationPrefix("/user");
        verify(brokerRegistry).setPreservePublishOrder(true);

        StompEndpointRegistry endpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);
        when(endpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOrigins(any(String[].class))).thenReturn(endpointRegistration);

        config.registerStompEndpoints(endpointRegistry);
        verify(endpointRegistry).addEndpoint("/ws");

        ChannelRegistration channelRegistration = mock(ChannelRegistration.class);
        config.configureClientInboundChannel(channelRegistration);
        verify(channelRegistration).interceptors(jwtInterceptor);
    }

    @Test
    void handlesEmptyOrigins() {
        WebSocketConfig config = new WebSocketConfig(jwtInterceptor, "");
        StompEndpointRegistry endpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);
        when(endpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOrigins(any(String[].class))).thenReturn(endpointRegistration);

        config.registerStompEndpoints(endpointRegistry);
        verify(endpointRegistry).addEndpoint("/ws");
    }
}
