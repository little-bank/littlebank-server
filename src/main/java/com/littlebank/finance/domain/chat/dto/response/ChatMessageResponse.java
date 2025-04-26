package com.littlebank.finance.domain.chat.dto.response;

import com.littlebank.finance.domain.chat.domain.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponse  {

    @Schema(description = "메시지 ID", example = "10")
    private Long id;

    @Schema(description = "보낸 사람 이름", example = "김동규")
    private String sender;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String message;

    @Schema(description = "메시지 타입", example = "TALK")
    private String type;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;
    @Schema(description = "메시지 생성 시간", example = "2024-04-23T13:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "아직 읽지 않은 사람 수", example = "1")
    private Integer unreadCount;

    public static ChatMessageResponse from(ChatMessage message, int unreadCount) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .sender(message.getSender().getName())
                .message(message.getMessage())
                .type(message.getType().name())
                .isRead(message.isRead())
                .unreadCount(unreadCount)
                .build();
    }
}