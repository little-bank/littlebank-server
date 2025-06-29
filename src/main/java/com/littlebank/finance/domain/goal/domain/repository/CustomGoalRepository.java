package com.littlebank.finance.domain.goal.domain.repository;

import com.littlebank.finance.domain.goal.domain.Goal;
import com.littlebank.finance.domain.goal.domain.GoalCategory;
import com.littlebank.finance.domain.goal.dto.response.ChildGoalResponse;
import com.littlebank.finance.domain.notification.dto.GoalAchievementNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomGoalRepository {
    Boolean existsCategorySameWeek(Long userId, GoalCategory category, LocalDateTime compareTargetDate);

    List<Goal> findByCreatedByAndWeekly(Long userId);

    Page<ChildGoalResponse> findChildWeeklyGoalResponses(Long familyId, Pageable pageable);
    List<ChildGoalResponse> findAllChildGoalResponses(Long familyId);
    List<GoalAchievementNotificationDto> findGoalAchievementNotificationDto();
}
