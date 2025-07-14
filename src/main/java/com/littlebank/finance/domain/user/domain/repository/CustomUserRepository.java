package com.littlebank.finance.domain.user.domain.repository;

import com.littlebank.finance.domain.user.dto.response.CommonUserInfoResponse;
import com.littlebank.finance.domain.user.dto.response.UserDetailsInfoResponse;
import com.littlebank.finance.domain.user.dto.response.UserSearchResponse;

import java.util.List;
import java.util.Optional;

public interface CustomUserRepository {
    Optional<UserDetailsInfoResponse> findUserDetailsInfo(Long targetUserId, Long userId);
    Optional<UserSearchResponse> findUserSearchResponse(Long requesterId, String phone);
    List<CommonUserInfoResponse> findSchoolUserBySchoolName(String schoolName);
}
