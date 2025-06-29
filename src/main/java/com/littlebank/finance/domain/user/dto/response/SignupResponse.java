package com.littlebank.finance.domain.user.dto.response;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String email;
    private UserRole role;
    private Authority authority;
    private Boolean isActive;

    public static SignupResponse of(User user, Boolean isActive) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .authority(user.getAuthority())
                .isActive(isActive)
                .build();
    }
}
