package com.littlebank.finance.domain.chat.dto;

import com.littlebank.finance.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class ChatMessageResponse  {
    private Long id;
    private String sender;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;

    public ChatMessageResponse(Long id, String sender, String message) {
        this.id = id;
        this.sender = sender;
        this.message = message;
    }

    public ChatMessageResponse(Long id, String sender, String message, String type, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getSender().getName(),
            message.getMessage(),
            message.getType().name(),
            message.isRead(),
            message.getCreatedAt()
        );
    }
}