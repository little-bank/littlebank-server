package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.ChatMessageResponse;
import com.littlebank.finance.domain.chat.entity.ChatMessage;
import com.littlebank.finance.domain.chat.service.ChatService;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.service.UserService;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ChatMessageController {
    private final ChatService chatService;
    private final UserService userService;
    public ChatMessageController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping("/chat.send.{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto dto) {
        //방 입장 권한 체크
        User sender = userService.findById(dto.getSenderId());
        User receiver = userService.findById(dto.getReceiverId());
        if (!chatService.isParticipant( roomId, sender.getId().toString())||
                !chatService.isParticipant(roomId, receiver.getId().toString())
        ) {
            throw new RuntimeException("이 채팅방 참여 불가능");
        }
        dto.setRoomId(roomId);
        ChatMessage saved=chatService.saveMessage(dto);
        log.info(" 메시지 브로드캐스트 대상 topic: /topic/chat/{}", roomId);
        return ChatMessageResponse.from(saved);
    }
}
