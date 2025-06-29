package com.littlebank.finance.domain.user.service;

import com.littlebank.finance.domain.friend.domain.repository.FriendRepository;
import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserConsent;
import com.littlebank.finance.domain.user.domain.repository.UserConsentRepository;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.dto.request.*;
import com.littlebank.finance.domain.user.dto.response.*;
import com.littlebank.finance.domain.user.exception.UserException;
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
    private final FriendRepository friendRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConsentRepository userConsentRepository;
    public SignupResponse saveUser(SignupRequest request, Authority authority) {
        User user = request.toEntity(authority);

        verifyDuplicatedEmail(user.getEmail());
        verifyDuplicatedPhone(user.getPhone());

        user.encodePassword(passwordEncoder);
        userRepository.save(user);

        UserConsent consent = request.toUserConsentEntity(user);
        userConsentRepository.save(consent);
        return SignupResponse.of(user, consent.getIsActive());
    }

    public ProfileImagePathUpdateResponse updateProfileImagePath(Long userId, String profileImagePath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateProfileImagePath(profileImagePath);

        return ProfileImagePathUpdateResponse.of(user);
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return MyInfoResponse.of(user);
    }

    @Transactional(readOnly = true)
    public UserDetailsInfoResponse getUserDetailsInfo(Long targetUserId, Long userId) {
        return userRepository.findUserDetailsInfo(targetUserId, userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    public MyInfoResponse updateMyInfo(Long userId, UserInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        User target = request.toEntity();

        if (userRepository.existsByEmailAndIdNot(target.getEmail(), user.getId())) {
            throw new UserException(ErrorCode.EMAIL_DUPLICATED);
        }

        String beforeName = user.getName();
        String afterName = request.getName();

        user.update(target);

        friendRepository.updateCustomName(userId, beforeName, afterName);

        return MyInfoResponse.of(user);
    }

    public UpdateStatusMessageResponse updateStatusMessage(Long userId, UpdateStatusMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateStatusMessage(request.getStatusMessage());

        return UpdateStatusMessageResponse.of(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        userRepository.deleteById(user.getId());
    }

    public SocialLoginAdditionalInfoResponse updateAdditionalInfoAfterSocialLogin(SocialLoginAdditionalInfoRequest request, long userId) {
        verifyDuplicatedPhone(request.getPhone());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateRequiredInfo(request.toEntity());

        return SocialLoginAdditionalInfoResponse.of(user);
    }

    @Transactional(readOnly = true)
    public UserSearchResponse searchUser(String phone, Long userId) {
        return userRepository.findUserSearchResponse(userId, phone)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public CommonUserInfoResponse searchUserByAccount(String bankCode, String account) {
        User target = userRepository.findByBankCodeAndBankAccount(bankCode, account)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return CommonUserInfoResponse.of(target);
    }

    public AccountPinResetResponse resetAccountPin(Long userId, AccountPinResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.resetPin(request.getPin());

        return AccountPinResetResponse.of(user);
    }

    private void verifyDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void verifyDuplicatedPhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new UserException(ErrorCode.PHONE_DUPLICATED);
        }
    }
}
