package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.ChatMessageResponse;
import com.littlebank.finance.domain.chat.entity.ChatMessage;
import com.littlebank.finance.domain.chat.service.ChatService;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@Tag(name = "채팅방 메세지 SEND API", description = "채팅방 메세지 전송 기능")
@RestController
@SecurityRequirement(name = "BearerAuth")
public class ChatMessageController {
    private final ChatService chatService;
    private final UserService userService;
    public ChatMessageController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping("/api-user/chat.send.{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto dto, Principal principal) {
        //방 입장 권한 체크
        //User sender = userService.findById(dto.getSenderId());
        //User receiver = userService.findById(dto.getReceiverId());
        //Long senderId=Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        //Long senderId=Long.valueOf(principal.getName());
        String email = principal.getName();
        User user = userService.findByEmail(email);
        Long senderId = user.getId();
        if (!chatService.isParticipant( roomId, senderId.toString())) {
            throw new RuntimeException("이 채팅방 참여자가 아닙니다.");
        }
        dto.setRoomId(roomId);
        dto.setSenderId(senderId);
        ChatMessage saved=chatService.saveMessage(dto);
        log.info(" 메시지 브로드캐스트 대상 topic: /topic/chat/{}", roomId);
        log.info("✅ DB 저장 완료: {}", saved.getMessage());
        return ChatMessageResponse.from(saved);
    }
}
