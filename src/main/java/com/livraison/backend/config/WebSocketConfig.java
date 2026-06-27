package com.livraison.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.*;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @PostConstruct
    public void logWsConfig() {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  [WS-CONFIG] WebSocket endpoints registered  ║");
        log.info("╠══════════════════════════════════════════════╣");
        log.info("║  /ws    → raw STOMP WebSocket (no SockJS)    ║");
        log.info("║  /chat  → STOMP over SockJS                  ║");
        log.info("║  App prefix  : /app                          ║");
        log.info("║  Broker      : /topic  /queue                ║");
        log.info("║  Heartbeat   : 10s in / 10s out              ║");
        log.info("╚══════════════════════════════════════════════╝");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        log.info("[WS-CONFIG] registered /ws  (raw WebSocket, binary-frame safe)");

        // SockJS fallback — used by the web browser client.
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        log.info("[WS-CONFIG] registered /chat (SockJS, web browser)");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                StompCommand command = accessor.getCommand();

                if (command == null) return message;

                switch (command) {
                    case CONNECT:
                        log.info("[WS-INTERCEPT] ◆ CONNECT  session={}", accessor.getSessionId());
                        break;

                    case SEND:
                        log.info("[WS-INTERCEPT] ◆ SEND  dest={}  session={}  content-type={}  body={}",
                                accessor.getDestination(),
                                accessor.getSessionId(),
                                accessor.getContentType(),
                                extractBody(message));
                        break;

                    case SUBSCRIBE:
                        log.info("[WS-INTERCEPT] ◆ SUBSCRIBE  dest={}  session={}  subId={}",
                                accessor.getDestination(),
                                accessor.getSessionId(),
                                accessor.getSubscriptionId());
                        break;

                    case DISCONNECT:
                        log.info("[WS-INTERCEPT] ◆ DISCONNECT  session={}", accessor.getSessionId());
                        break;

                    default:
                        break;
                }
                return message;
            }

            private String extractBody(Message<?> message) {
                Object payload = message.getPayload();
                if (payload instanceof byte[] bytes) {
                    String s = new String(bytes);
                    return s.length() > 200 ? s.substring(0, 200) + "…" : s;
                }
                return payload != null ? payload.toString() : "(null)";
            }
        });
    }
}
