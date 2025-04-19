package com.littlebank.finance.domain.chat.entity;

import com.littlebank.finance.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="chat_message")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage {
    public enum MessageType {ENTER, TALK, LEAVE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="room_id",nullable = false,length=100)
    private String roomId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sender_id",nullable = false)
    private User sender;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="receiver_id",nullable = false)
    private User receiver;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length=10)
    private MessageType type;

    @Builder.Default
    @Column(name="is_read", nullable = false)
    private boolean isRead=false;

    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
