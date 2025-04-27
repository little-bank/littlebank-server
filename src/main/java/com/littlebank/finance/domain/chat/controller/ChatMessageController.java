package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.response.ChatMessageResponse;
import com.littlebank.finance.domain.chat.dto.response.ReadMessageDto;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.chat.service.ChatService;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
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
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessageDto dto,
                            Principal principal) {
        log.info("💬 [서버 도착] @MessageMapping 호출됨: roomId={}", roomId);

        if (principal == null) {
            throw new ChatException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        ChatMessageResponse response = chatService.sendChatMessage(principal.getName(), roomId, dto);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);

        log.info("📢 메시지 전송 완료: roomId={}, sender={}", roomId, principal.getName());
    }

    @MessageMapping("/chat.read.{roomId}")
    public void readMessage(@DestinationVariable String roomId,
                            @Payload ReadMessageDto dto,
                            Principal principal) {
        log.info("📖 [읽음 처리] @MessageMapping 호출됨: roomId={}", roomId);

        if (principal == null) {
            throw new ChatException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        chatService.readChatMessage(principal.getName(), dto.getMessageId(), roomId);
        log.info("✅ 읽음 처리 완료: messageId={}, reader={}", dto.getMessageId(), principal.getName());
    }
}