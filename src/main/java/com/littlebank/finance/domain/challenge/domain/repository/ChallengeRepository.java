package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, CustomChallengeRepository {
    Page<Challenge> findByIsDeletedFalse(Pageable pageable);
}
