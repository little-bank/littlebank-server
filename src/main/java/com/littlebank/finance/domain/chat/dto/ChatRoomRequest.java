package com.littlebank.finance.domain.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomRequest {
    private String name;
    @Size(min=2, message="최소 두 명 이상의 참여자가 필요합니다.")
    private List<Long> participantIds;
}
