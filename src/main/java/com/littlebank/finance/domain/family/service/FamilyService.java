package com.littlebank.finance.domain.family.service;

import com.littlebank.finance.domain.family.domain.Family;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.family.domain.repository.FamilyMemberRepository;
import com.littlebank.finance.domain.family.domain.repository.FamilyRepository;
import com.littlebank.finance.domain.family.dto.request.FamilyMemberAddRequest;
import com.littlebank.finance.domain.family.dto.request.MyFamilyNicknameUpdateRequest;
import com.littlebank.finance.domain.family.dto.response.*;
import com.littlebank.finance.domain.family.exception.FamilyException;
import com.littlebank.finance.domain.friend.dto.response.FamilyInvitationAcceptResponse;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserRole;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.firebase.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FamilyService {
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FirebaseService firebaseService;
    private final NotificationRepository notificationRepository;

    public FamilyMemberAddResponse addFamilyMember(Long userId, FamilyMemberAddRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        // 요청자가 들어가있는 가족의 familyMember 데이터 조회
        FamilyMember member = familyMemberRepository.findByUserIdAndStatusWithFamily(user.getId(), Status.JOINED)
                .orElseGet(() -> {
                    // 없을 경우 가족 생성하고, 맴버 생성
                    Family newFamily = familyRepository.save(Family.builder()
                            .createdBy(user)
                            .build());

                    return familyMemberRepository.save(FamilyMember.builder()
                            .nickname(user.getName())
                            .family(newFamily)
                            .user(user)
                            .invitedBy(user)
                            .status(Status.JOINED)
                            .build());
                });

        Optional<FamilyMember> optionalTargetMember =
                familyMemberRepository.findByFamilyIdAndUserIdIncludingDeleted(
                        member.getFamily().getId(),
                        request.getTargetUserId()
                );

        FamilyMember targetMember;
        if (optionalTargetMember.isPresent()) {
            targetMember = optionalTargetMember.get();
            // 초대 요청 상태
            if (!targetMember.getIsDeleted() && targetMember.getStatus() == Status.REQUESTED) {
                throw new FamilyException(ErrorCode.FAMILY_INVITE_ALREADY_SENT);
            }
            // 이미 가입 된 상태
            if (!targetMember.getIsDeleted() && targetMember.getStatus() == Status.JOINED) {
                throw new FamilyException(ErrorCode.FAMILY_MEMBER_ALREADY_EXISTS);
            }
            // 초대 거절, 초대 취소 상태
            if (targetMember.getIsDeleted() && targetMember.getStatus() == Status.REQUESTED) {
                targetMember.reInvitation(user);
            }
            // 맴버 추방, 맴버 나가기 상태
            if (targetMember.getIsDeleted() && targetMember.getStatus() == Status.JOINED) {
                targetMember.reInvitation(user);
            }
        } else {
            targetMember = familyMemberRepository.save(FamilyMember.builder()
                    .nickname(targetUser.getName())
                    .family(member.getFamily())
                    .user(targetUser)
                    .invitedBy(user)
                    .status(Status.REQUESTED)
                    .build());
        }

        // 알림 생성
        try {
            Notification notification = notificationRepository.save(Notification.builder()
                    .receiver(user)
                    .message(user.getName() + "님이 가족 멤버로 초대했어요!")
                    .subMessage("수락하면 함께 활동할 수 있어요")
                    .type(NotificationType.ADD_FAMILY)
                    .targetId(targetMember.getId())
                    .isRead(false)
                    .build());
            log.info("알림 저장 성공: " + notification.getId());
            firebaseService.sendNotification(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return FamilyMemberAddResponse.of(targetMember);
    }

    @Transactional(readOnly = true)
    public FamilyInfoResponse getFamilyInfo(Long userId) {
        return familyMemberRepository.getFamilyInfoByUserId(userId);
    }

    public MyFamilyNicknameUpdateResponse updateMyFamilyNickname(MyFamilyNicknameUpdateRequest request) {
        FamilyMember familyMember = familyMemberRepository.findById(request.getFamilyMemberId())
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        familyMember.updateNickname(request.getNickname());

        return MyFamilyNicknameUpdateResponse.of(familyMember);
    }

    @Transactional(readOnly = true)
    public List<ReceivedFamilyInvitationResponse> getReceivedFamilyInvitations(Long userId) {
        List<FamilyMember> members = familyMemberRepository.findAllByUserIdAndStatus(userId, Status.REQUESTED);
        return members.stream()
                .map(member -> ReceivedFamilyInvitationResponse.of(member))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FamilyEnterCheckResponse checkFamilyMembership(Long userId) {
        if (familyMemberRepository.existsByUserIdAndStatus(userId, Status.JOINED)) {
            return FamilyEnterCheckResponse.of(Boolean.TRUE);
        }
        return FamilyEnterCheckResponse.of(Boolean.FALSE);
    }

    public FamilyInvitationAcceptResponse acceptFamilyInvitation(Long userId, Long familyMemberId) {
        familyMemberRepository.deleteByUserIdAndStatus(userId, Status.JOINED.name());

        FamilyMember member = familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        // 부모 2명 검증
        if (member.getUser().getRole() == UserRole.PARENT &&
                familyMemberRepository.findByMemberIdWithFamilyAndUser(familyMemberId).stream()
                        .filter(m -> m.getUser().getRole() == UserRole.PARENT).count() >= 2
        ) {
            throw new FamilyException(ErrorCode.MULTIPLE_PARENTS_NOT_ALLOWED);
        }

        member.accept();

        // 알림 생성
        try {
            member.getFamily().getMembers().stream()
                    .filter(m -> !m.getUser().getId().equals(userId) && m.getStatus() == Status.JOINED)
                    .forEach(m -> {
                        Notification notification = notificationRepository.save(Notification.builder()
                                .receiver(m.getUser())
                                .message(member.getNickname() + "님이 우리 가족이 되었어요!")
                                .type(NotificationType.ACCEPT_FAMILY)
                                .targetId(member.getId())
                                .isRead(false)
                                .build());
                        firebaseService.sendNotification(notification);
                    });
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }

        return FamilyInvitationAcceptResponse.of(member);
    }

    @Transactional(readOnly = true)
    public List<SentFamilyInvitationResponse> getSentFamilyInvitations(Long familyId) {
        List<FamilyMember> members = familyMemberRepository.findAllByFamilyIdAndStatusWithUser(familyId, Status.REQUESTED);
        return members.stream()
                .map(m -> SentFamilyInvitationResponse.of(m))
                .collect(Collectors.toList());
    }

    public void refuseFamilyInvitation(Long familyMemberId) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
                        .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        familyMemberRepository.deleteById(familyMember.getId());
    }

    public void cancelFamilyInvitation(Long familyMemberId) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        familyMemberRepository.deleteById(familyMember.getId());
    }

    public void forceOutMember(Long userId, Long familyMemberId) {
        FamilyMember member = familyMemberRepository.findByUserIdAndStatusWithUser(userId, Status.JOINED)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        // 부모 역할 권한 예외처리
        if (member.getUser().getRole() != UserRole.PARENT) {
            throw new FamilyException(ErrorCode.FORBIDDEN_PARENT_ONLY);
        }

        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        familyMemberRepository.deleteById(familyMember.getId());
    }

    public void leaveFamily(Long familyMemberId) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        familyMemberRepository.deleteById(familyMember.getId());
    }
}
