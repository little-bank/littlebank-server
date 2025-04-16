package com.littlebank.finance.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomRequest {

    private String name;
    private List<Long> participantIds;
}
