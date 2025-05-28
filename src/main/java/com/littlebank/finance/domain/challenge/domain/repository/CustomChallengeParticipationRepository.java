package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomChallengeParticipationRepository {
    Page<ChallengeParticipation> findOngoingParticipations(Long userId, Pageable pageable);
    Page<ChallengeParticipation> findCompletedParticipations(Long userId, Pageable pageable);

}
