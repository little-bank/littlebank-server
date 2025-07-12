package com.littlebank.finance.domain.subscription.domain;

import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "subscription")
@SQLDelete(sql = "UPDATE subscription SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    @Column(nullable = false)
    private int seat;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> members = new ArrayList<>();
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InviteCode> inviteCodes = new ArrayList<>();
    @Column(nullable = false, unique = true)
    private String purchaseToken;
    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    public Subscription(Long id, User owner, int seat, LocalDateTime startDate, LocalDateTime endDate, String purchaseToken, Boolean isDeleted) {
        this.id = id;
        this.owner = owner;
        this.seat = seat;
        this.startDate = startDate;
        this.endDate = endDate;
        this.purchaseToken = purchaseToken;
        this.isDeleted = isDeleted == null ? false : isDeleted;
    }

    public void addInviteCode(InviteCode inviteCode) {
        this.inviteCodes.add(inviteCode);
        inviteCode.setSubscription(this);
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
