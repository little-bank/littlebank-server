package com.littlebank.finance.domain.user.domain;

import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Table(name = "userConsent")
@SQLDelete(sql = "UPDATE userConsent SET is_deleted = true WHERE userConsent_id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor()
public class UserConsent extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userConsent_id")
    private Long id;
    @JoinColumn(nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private User user;
    @Column(nullable = false)
    private Boolean isActive;
    @Column(nullable = false)
    private Boolean isDeleted;
    @Builder
    private UserConsent(Long id, User user, Boolean isActive, Boolean isDeleted) {
        this.id = id;
        this.user = user;
        this.isActive = isActive;
        this.isDeleted = isDeleted == null ? false : isDeleted;
    }
}
