package com.littlebank.finance.domain.notification.domain;

import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "notification_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private NotificationType type;
    @Column(name = "target_id", nullable = true)
    private Long targetId;
    @Column(name = "message", length = 100, nullable = false)
    private String message;
    @Column(name = "sub_message", length = 100)
    private String subMessage;
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Builder
    public Notification(User receiver, NotificationType type, Long targetId, String message, String subMessage, boolean isRead) {
        this.receiver = receiver;
        this.type = type;
        this.targetId = targetId;
        this.message = message;
        this.subMessage = subMessage;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
