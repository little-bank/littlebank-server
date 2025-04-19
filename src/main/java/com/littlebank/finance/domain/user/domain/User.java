package com.littlebank.finance.domain.user.domain;

import com.littlebank.finance.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE user_id = ?")
@Where(clause = "is_deleted = false")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 11)
    private String phone;

    @Column(length = 6, nullable = false)
    private String rrn;

    @Column(length = 10)
    private String bankName;

    @Column(length = 3)
    private String bankCode;

    @Column(length = 20)
    private String bankAccount;

    @Column(length = 100)
    private String profileImageUrl;

    @Column(name = "balance")
    private Integer balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    @Column(nullable = false)
    private Boolean isSubscribe;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public User(
            Long id, String email, String password, String name, String phone, String rrn, String bankName,
            String bankCode, String bankAccount, String profileImageUrl, Integer balance, UserRole role,
            Authority authority, Boolean isSubscribe, LocalDateTime lastLoginAt, Boolean isDeleted
    ) {
        super(isDeleted);
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.rrn = rrn;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.bankAccount = bankAccount;
        this.profileImageUrl = profileImageUrl;
        this.balance = balance == null ? 0 : balance;
        this.role = role;
        this.authority = authority;
        this.isSubscribe = isSubscribe == null ? false : isSubscribe;
        this.lastLoginAt = lastLoginAt;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }
}
