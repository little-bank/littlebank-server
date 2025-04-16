package com.littlebank.finance.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomSummary {
    private String roomId;
    private String roomName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;

//    @QueryProjection

}
