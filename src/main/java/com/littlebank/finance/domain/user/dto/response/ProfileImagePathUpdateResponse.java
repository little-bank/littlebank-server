package com.littlebank.finance.domain.user.dto.response;

import com.littlebank.finance.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileImagePathUpdateResponse {
    private Long userId;
    private String profileImagePath;

    public static ProfileImagePathUpdateResponse of(User user) {
        return ProfileImagePathUpdateResponse.builder()
                .userId(user.getId())
                .profileImagePath(user.getProfileImagePath())
                .build();
    }
}
