package com.littlebank.finance.domain.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker //stomp 사용 명시
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {    @Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    log.info("register stomp endpoint 실행 완료");
    registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .addInterceptors(new StompAuthenticationInterceptor())
            .withSockJS();
}

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        log.info("configure message broker 실행 완료");
        registry.enableSimpleBroker("/topic"); //서버 -> 클라이언트
        registry.setApplicationDestinationPrefixes("/app"); //클라이언트 -> 서버
    }
}