package com.littlebank.finance.domain.user.service;

import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.dto.response.ProfileImagePathUpdateResponse;
import com.littlebank.finance.domain.user.dto.response.SignupResponse;
import com.littlebank.finance.domain.user.dto.response.UserInfoResponse;
import com.littlebank.finance.domain.user.excption.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse saveUser(User user) {
        verifyDuplicatedEmail(user.getEmail());

        user.encodePassword(passwordEncoder);

        return SignupResponse.of(userRepository.save(user));
    }

    public ProfileImagePathUpdateResponse updateProfileImagePath(long userId, String profileImagePath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateProfileImagePath(profileImagePath);

        return ProfileImagePathUpdateResponse.of(user);
    }

    public UserInfoResponse getMyInfo(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.of(user);
    }

    public UserInfoResponse updateMyInfo(long userId, User updateInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.update(updateInfo);

        return UserInfoResponse.of(user);
    }

    public void deleteUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        userRepository.deleteById(user.getId());
    }

    private void verifyDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(ErrorCode.EMAIL_DUPLICATED);
        }
    }
}
