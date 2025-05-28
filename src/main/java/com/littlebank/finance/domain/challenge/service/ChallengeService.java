package com.littlebank.finance.domain.challenge.service;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeParticipationRepository;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeRepository;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeRepositoryCustom;
import com.littlebank.finance.domain.challenge.dto.request.ChallengeUserRequestDto;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.littlebank.finance.domain.challenge.dto.response.ChallengeUserResponseDto;
import com.littlebank.finance.domain.challenge.exception.ChallengeException;
import com.littlebank.finance.domain.family.domain.Family;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.repository.FamilyMemberRepository;
import com.littlebank.finance.domain.family.domain.repository.FamilyRepository;
import com.littlebank.finance.domain.family.exception.FamilyException;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.common.PaginationPolicy;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.firebase.FirebaseService;
import com.littlebank.finance.global.redis.RedisPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final RedissonClient redissonClient;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ChallengeRepositoryCustom challengeRepositoryCustom;
    private final FamilyRepository familyRepository;
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;


    public ChallengeUserResponseDto joinChallenge(Long userId, Long challengeId, ChallengeUserRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.USER_NOT_FOUND));

        //redisson
        String lockKey = RedisPolicy.CHALLENGE_JOIN_KEY_PREFIX + challengeId;
        RLock lock = redissonClient.getLock(lockKey);

        String countKey = RedisPolicy.CHALLENGE_CURRENT_COUNT_KEY_PREFIX + challengeId;
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new ChallengeException(ErrorCode.FAIL_TO_GET_LOCK);
            }
            Challenge challenge = challengeRepository.findById(challengeId)
                    .orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));

            if (participationRepository.existsByChallengeIdAndUserId(challengeId, userId)) {
                throw new ChallengeException(ErrorCode.ALREADY_JOINED);
            }

            if (request.getStartDate().isBefore(challenge.getStartDate()) || request.getEndDate().isAfter(challenge.getEndDate())) {
                throw new ChallengeException(ErrorCode.INVALID_PARTICIPATION_PERIOD);
            }

            RAtomicLong counter = redissonClient.getAtomicLong(countKey);

            if (counter.isExists() == false) {
                counter.set(challenge.getCurrentParticipants());
            }
            long current = counter.incrementAndGet();
            if (current > challenge.getTotalParticipants()) {
                counter.decrementAndGet();
                throw new ChallengeException(ErrorCode.CHALLENGE_FULL);
            }


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime userStartDateTime = LocalDateTime.of(request.getStartDate(), request.getStartTime());

            ChallengeStatus challengeStatus;
            if (now.isAfter(request.getEndDate().atTime(23, 59, 59))) {
                challengeStatus = ChallengeStatus.FINISHED;
            } else {
                challengeStatus = ChallengeStatus.REQUESTED;
            }

            ChallengeParticipation participation;
            if (challenge.getCategory() == ChallengeCategory.WEEK) {
                participation = ChallengeParticipation.builder()
                        .challenge(challenge)
                        .user(user)
                        .challengeStatus(challengeStatus)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .subject(request.getSubject())
                        .title(challenge.getTitle())
                        .startTime(request.getStartTime())
                        .totalStudyTime(request.getTotalStudyTime())
                        .reward(request.getReward())
                        .isAccepted(false)
                        .isDeleted(false)
                        .build();
            } else {
                participation = ChallengeParticipation.builder()
                        .challenge(challenge)
                        .user(user)
                        .challengeStatus(challengeStatus)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .subject(challenge.getSubject())
                        .title(challenge.getTitle())
                        .startTime(request.getStartTime())
                        .totalStudyTime(request.getTotalStudyTime())
                        .reward(request.getReward())
                        .isAccepted(false)
                        .isDeleted(false)
                        .build();
            }

            participationRepository.save(participation);
            challenge.setCurrentParticipants(challenge.getCurrentParticipants() + 1);
            challengeRepository.save(challenge);

            // 알림
            Family family = familyRepository.findByUserIdWithMember(userId)
                    .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_NOT_FOUND));
            List<FamilyMember> parents = familyMemberRepository.findParentsByFamilyId(family.getId());
            try {
                for (FamilyMember parent : parents) {
                    Notification notification = notificationRepository.save(
                            Notification.builder()
                                    .receiver(parent.getUser())
                                    .message("우리 예쁜 "+ family.getMembers().get(0).getNickname() + "(이)가 목표를 신청했어요!")
                                    .type(NotificationType.CHALLENGE_JOIN)
                                    .targetId(challenge.getId())
                                    .isRead(false)
                                    .build());
                    firebaseService.sendNotification(notification);
                }
            } catch (DataIntegrityViolationException e) {
                log.warn("이미 동일한 알림이 존재합니다.");
            }
            
            return ChallengeUserResponseDto.of(participation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChallengeException(ErrorCode.LOCK_INTERUPPTED);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ChallengeAdminResponseDto> getAllChallenges(Long userId, ChallengeCategory challengeCategory, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page,
                PaginationPolicy.CHALLENGE_LIST_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<Challenge> challenges;
        if (challengeCategory != null) {
            challenges = challengeRepositoryCustom.findAllByCategory(challengeCategory, pageable);
        } else {
            challenges = challengeRepository.findByIsDeletedFalse(pageable);
        }

        List<ChallengeAdminResponseDto> responseList = challenges.stream()
                .map(challenge -> {
                    int currentActiveParticipants = participationRepository.countByChallengeIdAndStatuses(
                            challenge.getId(), List.of(ChallengeStatus.REQUESTED, ChallengeStatus.ACCEPT)
                    );
                    return ChallengeAdminResponseDto.of(challenge, currentActiveParticipants);
                })
                .toList();
        Page<ChallengeAdminResponseDto> responsePage = new PageImpl<>(responseList, pageable, challenges.getTotalElements());
        return CustomPageResponse.of(responsePage);
    }

    public ChallengeAdminResponseDto getChallengeDetail(Long userId, Long challengeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));

        challenge.increaseViewCount();
        int currentActiveParticipants = participationRepository.countByChallengeIdAndStatuses(
                challengeId, List.of(ChallengeStatus.REQUESTED, ChallengeStatus.ACCEPT)
        );
        return ChallengeAdminResponseDto.of(challenge, currentActiveParticipants);
    }

    public CustomPageResponse<ChallengeUserResponseDto> getMyChallenges(Long userId, String type, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page,
                PaginationPolicy.CHALLENGE_LIST_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdDate")
        );


        Page<ChallengeParticipation> participations;
        if ("ONGOING".equalsIgnoreCase(type)) {
            participations = participationRepository.findOngoingParticipations(userId, pageable);
        } else if ("COMPLETED".equalsIgnoreCase(type)) {
            participations = participationRepository.findCompletedParticipations(userId, pageable);
        } else {
            throw new ChallengeException(ErrorCode.UNVALID_MY_PARTICIPATION_TYPE);
        }

        List<ChallengeUserResponseDto> challengeList = participations.stream()
                .map(ChallengeUserResponseDto::of)
                .toList();

        Page<ChallengeUserResponseDto> responsePage = new PageImpl<>(challengeList, pageable, participations.getTotalElements());
        return CustomPageResponse.of(responsePage);

    }

    public List<ChallengeUserResponseDto> getChildInProgressChallenge(Long familyId, Long userId) {
        FamilyMember familyMember = familyMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        Long myFamilyId = familyMember.getFamily().getId();
        if (!myFamilyId.equals(familyId)) {
            throw new FamilyException(ErrorCode.FAMILY_NOT_FOUND);
        }
        List<ChallengeParticipation> participations = challengeParticipationRepository.findChildrenParticipationByFamilyId
                (familyId, List.of(ChallengeStatus.REQUESTED, ChallengeStatus.ACCEPT));
        return participations.stream().map(ChallengeUserResponseDto::of).toList();
    }

    public ChallengeUserResponseDto acceptApplyChallenge(Long participationId, Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        ChallengeParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.NOT_FOUND_PARTICIPATION));

        if(participation.getEndDate().isBefore(LocalDate.now())) {
            throw new ChallengeException(ErrorCode.CHALLENGE_END_DATE_EXPIRED);
        }
        if (!participation.getIsAccepted()) participation.setIsAccepted(true);
        else throw new ChallengeException(ErrorCode.ALREADY_ACCEPT);

        participation.setChallengeStatus(ChallengeStatus.ACCEPT);
        return ChallengeUserResponseDto.of(participation);
    }
}