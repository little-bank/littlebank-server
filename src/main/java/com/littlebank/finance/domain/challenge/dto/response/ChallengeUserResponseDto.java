package com.littlebank.finance.domain.challenge.dto.response;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeUserResponseDto {
    private Long participationId;
    private Long challengeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String subject;
    private ChallengeStatus challengeStatus;
    private LocalTime startTime;
    private Integer totalStudyTime;
    private Integer reward;
    private boolean isAccepted;

    public static ChallengeUserResponseDto of(ChallengeParticipation participation) {
        return ChallengeUserResponseDto.builder()
                .participationId(participation.getId())
                .challengeId(participation.getChallenge().getId())
                .startDate(participation.getStartDate())
                .endDate(participation.getEndDate())
                .title(participation.getTitle())
                .subject(participation.getSubject())
                .challengeStatus(participation.getChallengeStatus())
                .startTime(participation.getStartTime())
                .totalStudyTime(participation.getTotalStudyTime())
                .reward(participation.getReward())
                .isAccepted(participation.getIsAccepted())
                .build();
    }
}
