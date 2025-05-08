package com.littlebank.finance.domain.family.domain;

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
@SQLDelete(sql = "UPDATE family_member SET is_deleted = true WHERE member_id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    @Column(length = 20, nullable = false)
    private String nickname;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family", nullable = false)
    private Family family;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    public FamilyMember(String nickname, Family family, User user, Status status, Boolean isDeleted) {
        this.nickname = nickname;
        this.family = family;
        this.user = user;
        this.status = status;
        this.isDeleted = isDeleted == null ? false : isDeleted;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
