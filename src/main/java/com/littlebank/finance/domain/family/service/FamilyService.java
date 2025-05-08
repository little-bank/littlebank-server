package com.littlebank.finance.domain.family.service;

import com.littlebank.finance.domain.family.domain.Family;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.family.domain.repository.FamilyMemberRepository;
import com.littlebank.finance.domain.family.domain.repository.FamilyRepository;
import com.littlebank.finance.domain.family.dto.request.FamilyMemberAddRequest;
import com.littlebank.finance.domain.family.dto.request.MyFamilyNicknameUpdateRequest;
import com.littlebank.finance.domain.family.dto.response.FamilyMemberAddResponse;
import com.littlebank.finance.domain.family.dto.response.MyFamilyNicknameUpdateResponse;
import com.littlebank.finance.domain.family.exception.FamilyMemberException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public FamilyMemberAddResponse addFamilyMember(Long userId, FamilyMemberAddRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        // 요청자의 familyMember 데이터 조회
        FamilyMember member = familyMemberRepository.findByUserIdWithFamily(userId)
                .orElseGet(() -> {
                    // 없을 경우 가족 생성하고, 맴버 생성
                    Family newFamily = familyRepository.save(Family.builder()
                            .createdBy(user)
                            .build());

                    return familyMemberRepository.save(FamilyMember.builder()
                            .nickname(user.getName())
                            .family(newFamily)
                            .user(user)
                            .status(Status.JOINED)
                            .build());
                });

        // 가족에 이미 소속되어 있는 맴버인지 검증
        familyMemberRepository.findByFamilyIdAndUserId(member.getFamily().getId(), request.getTargetUserId())
                .ifPresent(targetMember -> {
                    if (targetMember.getStatus() == Status.REQUESTED) {
                        throw new FamilyMemberException(ErrorCode.FAMILY_INVITE_ALREADY_SENT);
                    }
                    if (targetMember.getStatus() == Status.JOINED) {
                        throw new FamilyMemberException(ErrorCode.FAMILY_MEMBER_ALREADY_EXISTS);
                    }
                });

        // 맴버 요청 상태로 저장
        FamilyMember newMember = familyMemberRepository.save(FamilyMember.builder()
                .nickname(targetUser.getName())
                .family(member.getFamily())
                .user(targetUser)
                .status(Status.REQUESTED)
                .build());

        return FamilyMemberAddResponse.of(newMember);
    }

    public MyFamilyNicknameUpdateResponse updateMyFamilyNickname(MyFamilyNicknameUpdateRequest request) {
        FamilyMember familyMember = familyMemberRepository.findById(request.getFamilyMemberId())
                .orElseThrow(() -> new FamilyMemberException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        familyMember.updateNickname(request.getNickname());

        return MyFamilyNicknameUpdateResponse.of(familyMember);
    }
}
