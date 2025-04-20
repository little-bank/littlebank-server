package com.littlebank.finance.domain.user.dto.response;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String bankName;
    private String bankCode;
    private String bankAccount;
    private String profileImagePath;
    private UserRole role;
    private Authority authority;
    private boolean isSubscribe;
    private LocalDateTime lastLoginAt;

    public static UserInfoResponse of(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .bankName(user.getBankName())
                .bankCode(user.getBankCode())
                .bankAccount(user.getBankAccount())
                .profileImagePath(user.getProfileImagePath())
                .role(user.getRole())
                .authority(user.getAuthority())
                .isSubscribe(user.getIsSubscribe())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
