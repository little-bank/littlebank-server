package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.chat.service.ChatService;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@Tag(name = "채팅방 메세지 SEND API", description = "채팅방 메세지 전송 기능")
@RestController
@RequestMapping("/api-user/chat")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat.send.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto dto, Principal principal) {
        log.info("💬 [서버 도착] @MessageMapping 호출됨: roomId={}", roomId);

        if (principal == null) {
            log.warn("🚫 Principal이 null입니다. 인증되지 않은 사용자");
            throw new ChatException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String email = principal.getName(); // 로그인한 사람 email
        log.info("✅ Principal 이메일: {}", email);

        // 이메일로 DB 조회해서 tokenUserId 가져오기
        Long tokenUserId = chatService.getUserIdByEmail(email);

        if (dto.getSenderId() == null || !dto.getSenderId().equals(tokenUserId)) {
            log.warn("🚫 인증된 사용자 ID와 메시지의 senderId 불일치: tokenUserId={}, dtoSenderId={}", tokenUserId, dto.getSenderId());
            throw new ChatException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        dto.setSenderId(tokenUserId); // 서버에서 강제 세팅

        chatService.handleChatMessage(roomId, dto, email);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, dto);
        log.info("📢 메시지 전송 완료: roomId={}", roomId);
    }
}
