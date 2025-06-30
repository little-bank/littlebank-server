package com.littlebank.finance.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionAchievementNotificationDto {
    private Long parentId;
    private String childNickname;
    private String missionTitle;
    private Long missionId;
}
