package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.notification.dto.response.ChallengeAchievementNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomChallengeParticipationRepository {
    Page<ChallengeParticipation> findOngoingParticipations(Long userId, Pageable pageable);
    Page<ChallengeParticipation> findCompletedParticipations(Long userId, Pageable pageable);
    List<ChallengeParticipation> updateExpiredChallengesToAchievement();
    List<ChallengeAchievementNotificationDto> findChallengeAchievementNotificationDto();

}
