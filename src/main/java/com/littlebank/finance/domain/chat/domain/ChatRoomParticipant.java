package com.littlebank.finance.domain.chat.domain;

import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="chat_room_participant",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="room_id",nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;
}