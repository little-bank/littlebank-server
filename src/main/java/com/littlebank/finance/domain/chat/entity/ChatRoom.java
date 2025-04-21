package com.littlebank.finance.domain.chat.entity;

import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name="chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {
    @Id
    @Column(length=100)
    private String id;

    @Column(name="room_name",nullable = false,length = 50)
    private String name;
}