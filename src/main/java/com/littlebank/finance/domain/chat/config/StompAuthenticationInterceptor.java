package com.littlebank.finance.domain.chat.config;


import com.littlebank.finance.global.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthenticationInterceptor implements HandshakeInterceptor {
    private final TokenProvider tokenProvider;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String authorizationHeader = httpServletRequest.getHeader("Authorization");
            log.info("📦 받은 Authorization 헤더: {}", authorizationHeader);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (tokenProvider.validateToken(token)) {
                    Long userId = Long.valueOf(tokenProvider.getAuthentication(token).getName());
                    attributes.put("userId", userId); // 이후 WebSocket 메시지 처리 시 사용 가능
                    log.info("✅ WebSocket JWT 인증 성공 - userId: {}", userId);
                    return true;
                } else {
                    log.warn("❌ WebSocket JWT 인증 실패 - 유효하지 않은 토큰");
                }
            } else {
                log.warn("❌ WebSocket JWT 인증 실패 - Authorization 헤더 없음");
            }
        }
        return false;
    }



    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 연결 후 처리

    }
}