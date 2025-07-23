package com.littlebank.finance.domain.challenge.service;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeParticipationRepository;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeRepository;
import com.littlebank.finance.domain.challenge.dto.request.ChallengeFinishScoreRequestDto;
import com.littlebank.finance.domain.challenge.dto.request.ChallengeUserRequestDto;
import com.littlebank.finance.domain.challenge.dto.response.ChallengeFinishScoreResponseDto;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.littlebank.finance.domain.challenge.dto.response.ChallengeUserResponseDto;
import com.littlebank.finance.domain.challenge.exception.ChallengeException;
import com.littlebank.finance.domain.family.domain.Family;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
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
                throw new ChallengeException(ErrorCode.ALREADY_JOINED_CHALLENGE);
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


            ChallengeParticipation participation = ChallengeParticipation.builder()
                    .challenge(challenge)
                    .user(user)
                    .challengeStatus(ChallengeStatus.REQUESTED)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .subject(challenge.getSubject())
                    .title(challenge.getTitle())
                    .startTime(request.getStartTime())
                    .totalStudyTime(request.getTotalStudyTime())
                    .reward(request.getReward())
                    .isRewarded(false)
                    .isAccepted(false)
                    .isDeleted(false)
                    .build();

            participationRepository.save(participation);
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
                                    .message(family.getMembers().get(0).getNickname() + "(이)가 챌린지 승인을 요청했어요!")
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
    public CustomPageResponse<ChallengeAdminResponseDto> getAllChallenges(ChallengeCategory challengeCategory, int page) {
        Pageable pageable = PageRequest.of(page,
                PaginationPolicy.CHALLENGE_LIST_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<Challenge> challenges;
        if (challengeCategory != null) {
            challenges = challengeRepository.findAllByCategory(challengeCategory, pageable);
        } else {
            challenges = challengeRepository.findByIsDeletedFalse(pageable);
        }

        List<ChallengeAdminResponseDto> responseList = challenges.stream()
                .map(challenge -> {
                    int currentActiveParticipants = participationRepository.countByChallengeIdAndStatuses(
                            challenge.getId(), List.of(ChallengeStatus.ACCEPT)
                    );
                    return ChallengeAdminResponseDto.of(challenge, currentActiveParticipants);
                })
                .toList();
        Page<ChallengeAdminResponseDto> responsePage = new PageImpl<>(responseList, pageable, challenges.getTotalElements());
        return CustomPageResponse.of(responsePage);
    }


    public CustomPageResponse<ChallengeUserResponseDto> getMyChallenges(Long userId, String type, int page) {
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

    public CustomPageResponse<ChallengeUserResponseDto> getChildChallenges(Long familyId, Long childId, Long userId, int page) {
        FamilyMember familyMember = familyMemberRepository.findByUserIdAndStatusWithUser(userId, Status.JOINED)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        Long myFamilyId = familyMember.getFamily().getId();
        if (!myFamilyId.equals(familyId)) {
            throw new FamilyException(ErrorCode.FAMILY_NOT_FOUND);
        }
        FamilyMember childMember = familyMemberRepository.findByUserIdAndStatusWithUser(childId, Status.JOINED)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page,
                PaginationPolicy.CHALLENGE_LIST_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdDate")
        );
        Page<ChallengeParticipation> participations = challengeParticipationRepository.findByUserId(childMember.getUser().getId(), pageable);


        Page<ChallengeUserResponseDto> responsePage = participations.map(ChallengeUserResponseDto::of);
        return CustomPageResponse.of(responsePage);
    }

    public ChallengeUserResponseDto acceptApplyChallenge(Long participationId, Long parentId) {
        ChallengeParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.NOT_FOUND_PARTICIPATION));
        FamilyMember parentMember = familyMemberRepository.findByUserIdAndStatusWithUser(parentId, Status.JOINED)
                .orElseThrow(() -> new FamilyException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));

        Challenge challenge = participation.getChallenge();
        if(participation.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ChallengeException(ErrorCode.CHALLENGE_END_DATE_EXPIRED);
        }
        if (!participation.getIsAccepted()) participation.setIsAccepted(true);
        else throw new ChallengeException(ErrorCode.ALREADY_CHALLENGE_ACCEPT);

        participation.setChallengeStatus(ChallengeStatus.ACCEPT);
        participation.setParent(parentMember);
        challenge.setCurrentParticipants(challenge.getCurrentParticipants() + 1);
        challengeRepository.save(challenge);

        // 알림
        try {
            Notification notification = notificationRepository.save(Notification.builder()
                            .receiver(participation.getUser())
                            .message("우리 부모님(" + parentMember.getNickname() + ")이 챌린지를 승낙했습니다!")
                            .type(NotificationType.CHALLENGE_ACCEPT)
                            .targetId(participation.getId())
                            .isRead(false)
                    .build());
            log.info("알림 저장 성공: " + notification.getId());
            log.info("알림 내용: " + notification);
            firebaseService.sendNotification(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }

        return ChallengeUserResponseDto.of(participation);
    }

    public ChallengeUserResponseDto getChildRequestedChallenge(Long parentId, Long participationId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        ChallengeParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.NOT_FOUND_PARTICIPATION));
        if (participation.getChallengeStatus().equals(ChallengeStatus.ACCEPT)) {
            throw new ChallengeException(ErrorCode.NOT_REQUESTED_CHALLENGE);
        }
        return ChallengeUserResponseDto.of(participation);
    }

    public ChallengeFinishScoreResponseDto applyChallengeScore(Long participationId, ChallengeFinishScoreRequestDto request) {
        ChallengeParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.NOT_FOUND_PARTICIPATION));
        if (!participation.getChallengeStatus().equals(ChallengeStatus.ACHIEVEMENT)) {
            throw new ChallengeException(ErrorCode.CHALLENGE_NOT_FINISH);
        }
        if (request.getScore() < 0 || request.getScore() > 100) {
            throw new ChallengeException(ErrorCode.INVALID_INPUT_VALUE);
        }
        participation.storeScore(request.getScore());
        participationRepository.save(participation);
        return ChallengeFinishScoreResponseDto.of(participation, request.getScore());
    }
}