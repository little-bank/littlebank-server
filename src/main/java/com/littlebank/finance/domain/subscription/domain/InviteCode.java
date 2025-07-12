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


@Entity
@Getter
@Table(name = "invitecode")
@SQLDelete(sql = "UPDATE invitecode SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteCode extends BaseEntity {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private boolean used;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    @OneToOne
    private User redeemedBy;
    @Column(nullable = false)
    private Boolean isDeleted;


    @Builder
    public InviteCode(String code, boolean used, Subscription subscription, Boolean isDeleted) {
        this.code = code;
        this.used = used;
        this.subscription = subscription;
        this.isDeleted = isDeleted == null ? false : isDeleted;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void setIsUsed() {
        this.used = true;
    }

    public void setRedeemedBy(User user) {
        this.redeemedBy = user;
    }
    public boolean isUsed() {
        return this.used;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
