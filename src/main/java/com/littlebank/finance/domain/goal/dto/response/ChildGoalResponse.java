package com.littlebank.finance.domain.goal.dto.response;

import com.littlebank.finance.domain.goal.domain.GoalCategory;
import com.littlebank.finance.domain.goal.domain.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChildGoalResponse {
    private Long goalId;
    private String title;
    private GoalCategory category;
    private Integer reward;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GoalStatus status;
    private Long familyMemberId;
    private String childNickname;
    private Boolean isRewarded;
    private Integer minStampCount;
    private Integer stampCount;
}
