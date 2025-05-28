package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.ChallengeParticipation;
import com.littlebank.finance.domain.challenge.domain.ChallengeStatus;
import com.littlebank.finance.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long>, CustomChallengeParticipationRepository {
    boolean existsByChallengeIdAndUserId(Long challengeId, Long userId);

    @Query("SELECT COUNT(cp) FROM ChallengeParticipation cp " +
            "WHERE cp.challenge.id = :challengeId " +
            "AND cp.challengeStatus IN (:statuses) " +
            "AND cp.isDeleted = false")
    int countByChallengeIdAndStatuses(@Param("challengeId") Long challengeId,
                                      @Param("statuses") List<ChallengeStatus> challengeStatuses);

    @Query("""
        SELECT cp
        FROM ChallengeParticipation cp
        JOIN cp.user u
        JOIN FamilyMember fm ON fm.user = u
        WHERE fm.family.id = :familyId
        AND fm.status = 'JOINED'
          AND u.role = 'CHILD'
          AND cp.challengeStatus IN :statuses
    """)
    List<ChallengeParticipation> findChildrenParticipationByFamilyId(
            @Param("familyId") Long familyId,
            @Param("statuses") List<ChallengeStatus> statuses
    );
}
