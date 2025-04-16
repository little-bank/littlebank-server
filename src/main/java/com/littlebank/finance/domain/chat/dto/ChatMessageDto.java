package com.littlebank.finance.domain.chat.dto;

import com.littlebank.finance.domain.chat.entity.ChatMessage;
import com.littlebank.finance.domain.user.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto {
    private String roomId;
    private Long senderId;
    private Long receiverId;
    private String message;
    private ChatMessage.MessageType type;
}
