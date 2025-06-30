package com.littlebank.finance.global.config.scheduler;

import com.littlebank.finance.domain.goal.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {
    private final FixPushNotificationService fixPushNotificationService;

    /**
     * [[부모]목표 달성 알림]
     * 매주 월요일 오전 8시
     * 부모에게 아이들의 달성한 목표에 대해 포인트 지급에 대한 알림
     */
    @Scheduled(cron = "0 0 8 * * 1")
    public void notifyParentOfChildGoalAchievements() {
        fixPushNotificationService.sendWeeklyGoalAchievementAlertToParents();
    }

    /**
     * [[아이] 목표 제안 알림]
     * 매주 월요일 오전 8시
     * 아이에게 새로운 목표에 도전하라는 알림
     */
    @Scheduled(cron = "0 0 8 * * 1")
    public void suggestNewWeeklyGoalToChildren() {
        fixPushNotificationService.suggestNewWeeklyGoalToChildren();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateStatusAtMidnight() {
        fixPushNotificationService.updateExpiredMissionsAndChallengesStatus();
    }

    /**
     * [미션 및 챌린지 달성 알림]
     * 매일 오전 9시
     * 부모에게 아이들의 달성한 미션에 대해 포인트 지급에 대한 알림
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void notifyParentsOfExpiredMissions() {
        fixPushNotificationService.notifyParentsOfCompletedMissions();
    }

    /**
     * [미션 및 챌린지 달성 알림]
     * 매일 오전 9시
     * 부모에게 아이들의 달성한 챌린지에 대해 포인트 지급에 대한 알림
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void notifyParentsOfExpiredChallenges() {
        fixPushNotificationService.notifyParentsOfCompletedChallenges();
    }

    /**
     *
     * [[부모] 미션 주기 제안 알림]
     * 월요일 18시 30분
     * 아이에게 미션 제안하라는 알림
     */
    @Scheduled(cron = "0 30 18 * * 1")
    public void suggestNewMissionToParents() {
        fixPushNotificationService.suggestParentsMissionCreation();
    }

    /**
     *
     * [[아이] 챌린지 참여하기 제안 알림]
     * 일요일 20시
     * 아이에게 챌린지 참여하라는 알림
     */
    @Scheduled(cron = "0 0 20 * * 7")
    public void suggestChallengeToChildren() {
        fixPushNotificationService.suggestChildrenParticipateChallenge();
    }

}
