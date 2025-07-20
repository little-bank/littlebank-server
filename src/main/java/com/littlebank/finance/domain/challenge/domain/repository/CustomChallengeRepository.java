package com.littlebank.finance.domain.challenge.domain.repository;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CustomChallengeRepository {
    Page<Challenge> findAllByCategory(ChallengeCategory challengeCategory, Pageable pageable);
    List<ChallengeAdminResponseDto> getAllChallenges(ChallengeCategory challengeCategory);
}
