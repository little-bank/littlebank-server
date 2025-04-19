package com.littlebank.finance.domain.chat.config;


import com.littlebank.finance.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (tokenProvider.validateToken(token)) {
                    Authentication auth = tokenProvider.getAuthentication(token);
                    accessor.setUser(auth);
                    log.info("✅ STOMP 연결 인증 성공 - user: {}", auth.getName());
                } else {
                    log.warn("❌ STOMP 연결 인증 실패 - 유효하지 않은 토큰");
                    throw new AccessDeniedException("유효하지 않은 토큰입니다.");
                }
            } else {
                log.warn("❌ STOMP 연결 인증 실패 - Authorization 헤더 없음");
                throw new AccessDeniedException("토큰이 필요합니다.");
            }
        }

        return message;
    }
}
