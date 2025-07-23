package com.littlebank.finance.domain.challenge.dto.response.admin;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ChallengeAdminResponseDto {

    private Long id;
    private String title;
    private String category;
    private String subject;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer currentParticipants;
    private Integer totalParticipants;
    private int viewCount;

    public static ChallengeAdminResponseDto of(Challenge challenge, int currentActiveParticipants) {
        return ChallengeAdminResponseDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .category(challenge.getCategory().name())
                .subject(challenge.getSubject())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .currentParticipants(currentActiveParticipants)
                .totalParticipants(challenge.getTotalParticipants())
                .viewCount(challenge.getViewCount())
                .build();
    }
}