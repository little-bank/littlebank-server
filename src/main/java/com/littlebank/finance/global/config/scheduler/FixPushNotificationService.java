package com.littlebank.finance.global.config.scheduler;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeParticipationRepository;
import com.littlebank.finance.domain.family.domain.FamilyMember;
import com.littlebank.finance.domain.family.domain.Status;
import com.littlebank.finance.domain.family.domain.repository.FamilyMemberRepository;
import com.littlebank.finance.domain.goal.domain.repository.GoalRepository;
import com.littlebank.finance.domain.mission.domain.Mission;
import com.littlebank.finance.domain.mission.domain.repository.MissionRepository;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.notification.dto.GoalAchievementNotificationDto;
import com.littlebank.finance.domain.notification.dto.response.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixPushNotificationService {
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;
    private final MissionRepository missionRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final FamilyMemberRepository familyMemberRepository;
    public List<GoalAchievementNotificationDto> sendWeeklyGoalAchievementAlertToParents() {
        List<GoalAchievementNotificationDto> results = goalRepository.findGoalAchievementNotificationDto();
        try {
            results.stream()
                    .forEach(r -> {
                        User parent = userRepository.findById(r.getParentId())
                                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

                        Notification notification = notificationRepository.save(Notification.builder()
                                .receiver(parent)
                                .message("지난 주, " + r.getNickname() + "(이)가 \"" + r.getTitle() + "\" 목표를 " + (r.getStampCount() * 100 / 7) + "% 달성했어요!")
                                .subMessage("앱에서 아이에게 약속한 보상을 주세요~!")
                                .type(NotificationType.GOAL_ACHIEVEMENT)
                                .targetId(r.getGoalId())
                                .isRead(false)
                                .build());
                        firebaseService.sendNotification(notification);
                    });
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return results;
    }

    public List<SuggestChildDto> suggestNewWeeklyGoalToChildren() {
        List<User> children = userRepository.findAllByRoleAndIsDeletedFalse(UserRole.CHILD);
        List<SuggestChildDto> results = new ArrayList<>();
        try {
            children.forEach(child -> {
                Notification notification = notificationRepository.save(Notification.builder()
                        .receiver(child)
                        .message(child.getName() + "님, 이번 주에도 새로운 목표에 도전하세요!")
                        .subMessage("달성하고 보상을 받아보세요!")
                        .type(NotificationType.SUGGEST_NEW_GOAL)
                        .isRead(false)
                        .build());
                firebaseService.sendNotification(notification);

                results.add(new SuggestChildDto(
                        child.getId(),
                        child.getName()
                ));
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 알림 생략 또는 전송 실패가 발생했습니다.");
        }
        return results;
    }

    public void updateExpiredMissionsAndChallengesStatus() {
        List<Mission> expiredMissions = missionRepository.updateExpiredMissionsToAchievement();
        List<ChallengeParticipation> expiredChallenges = challengeParticipationRepository.updateExpiredChallengesToAchievement();
        log.info("상태 갱신 완 - 미션 {}개, 챌린지 {}개", expiredMissions.size(), expiredChallenges.size());
        missionRepository.saveAll(expiredMissions);
        challengeParticipationRepository.saveAll(expiredChallenges);
    }

    public List<MissionAchievementNotificationDto> notifyParentsOfCompletedMissions() {
        // 완료된 미션 알림
        List<MissionAchievementNotificationDto> missionResults = missionRepository.findMissionAchievementNotificationDto();
        log.info("완료된 미션 알림 개수: ", missionResults.size());
        try {
            missionResults.stream().forEach(
                    r -> {
                        User parent = userRepository.findById(r.getParentId())
                                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
                        Notification notification = notificationRepository.save(Notification.builder()
                                .receiver(parent)
                                .message(r.getChildNickname() + "(이)가 미션 '" + r.getMissionTitle() + "'을 완료했어요!🎉")
                                .subMessage("우리 아이를 칭찬하러 가볼까요?")
                                .type(NotificationType.MISSION_ACHIEVEMENT)
                                .targetId(r.getMissionId())
                                .isRead(false)
                                .build());
                        firebaseService.sendNotification(notification);
                    }
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return missionResults;
    }


    public List<ChallengeAchievementNotificationDto> notifyParentsOfCompletedChallenges() {
        List<ChallengeAchievementNotificationDto> challengeResults = challengeParticipationRepository.findChallengeAchievementNotificationDto();
        log.info("완료된 챌린지 개수: ", challengeResults.size());
        try {
            challengeResults.stream().forEach(
                    r -> {
                        User parent = userRepository.findById(r.getParentId())
                                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
                        Notification notification = notificationRepository.save(Notification.builder()
                                .receiver(parent)
                                .message(r.getChildNickname() + "가 챌린지 '" + r.getChallengeTitle() + "'을 완료했어요!🎉")
                                .subMessage("우리 아이를 칭찬하러 가볼까요?")
                                .type(NotificationType.CHALLENGE_ACHIEVEMENT)
                                .targetId(r.getChallengeId())
                                .isRead(false)
                                .build());
                        firebaseService.sendNotification(notification);
                    }
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 동일한 알림이 존재합니다.");
        }
        return challengeResults;
    }


    public List<SuggestParentDto> suggestParentsMissionCreation() {
        List<User> parents = userRepository.findAllByRoleAndIsDeletedFalse(UserRole.PARENT);
        List<SuggestParentDto> results = new ArrayList<>();

        for (User parent : parents) {
            // 부모가 JOINED 상태로 속한 가족을 조회
            Optional<FamilyMember> parentFamilyOpt =
                    familyMemberRepository.findByUserIdAndStatusWithFamily(parent.getId(), Status.JOINED);
            if (parentFamilyOpt.isEmpty()) continue;
            Long familyId = parentFamilyOpt.get().getFamily().getId();

            // 가족의 JOINED 자녀 조회
            List<FamilyMember> children = familyMemberRepository.findChildrenByParentUserId(parent.getId());

            for (FamilyMember child : children) {
                try {
                    Notification notification = notificationRepository.save(Notification.builder()
                            .receiver(parent)
                            .message(child.getNickname() + "에게 새로운 미션을 주세요!")
                            .type(NotificationType.SUGGEST_MISSION_CREATION)
                            .isRead(false)
                            .build());
                    firebaseService.sendNotification(notification);
                    results.add(new SuggestParentDto(
                            parent.getId(),
                            child.getNickname(),
                            String.valueOf(child.getUser().getId())
                    ));
                } catch (DataIntegrityViolationException e) {
                    log.warn("중복 알림 생략 - parentId: {}, childId: {}", parent.getId(), child.getUser().getId());
                }
            }
        }
        return results;
    }

    public List<SuggestChildDto> suggestChildrenParticipateChallenge() {
        List<User> children = userRepository.findAllByRoleAndIsDeletedFalse(UserRole.CHILD);
        List<SuggestChildDto> results = new ArrayList<>();
        try {
            children.forEach(child -> {
                Notification notification = notificationRepository.save(Notification.builder()
                        .receiver(child)
                        .message(child.getName() + "님, 이번 주 새로운 챌린지가 열렸어요!")
                        .subMessage("도전하고 보상을 받아보세요!")
                        .type(NotificationType.SUGGEST_CHALLENGE_PARTICIPATION)
                        .isRead(false)
                        .build());
                firebaseService.sendNotification(notification);

                results.add(new SuggestChildDto(
                        child.getId(),
                        child.getName()
                ));
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 알림 생략 또는 전송 실패가 발생했습니다.");
        }
        return results;

    }
}
