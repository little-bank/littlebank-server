package com.littlebank.finance.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomCreateRequest {

    @Schema(description = "채팅방 이름", example = "스터디방")
    private String name;

    @Schema(description = "참여자 ID 목록", example = "[1, 2]")
    private List<Long> participantIds;
}