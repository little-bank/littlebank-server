package com.littlebank.finance.domain.challenge.service.admin;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeRepository;
import com.littlebank.finance.domain.challenge.dto.request.admin.ChallengeAdminRequestDto;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.littlebank.finance.domain.challenge.exception.ChallengeException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class AdminChallengeService {
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    // ADMIN
    public ChallengeAdminResponseDto createChallenge(Long adminId, ChallengeAdminRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.USER_NOT_FOUND));
        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .category(ChallengeCategory.valueOf(request.getCategory()))
                .subject(request.getSubject())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalParticipants(request.getTotalParticipants())
                .currentParticipants(0)
                .build();
        Challenge savedChallenge = challengeRepository.save(challenge);
        return ChallengeAdminResponseDto.of(savedChallenge, 0);
    }

    public ChallengeAdminResponseDto updateChallenge(Long adminId, Long challengeId, ChallengeAdminRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.USER_NOT_FOUND));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));

        challenge.update(
                request.getTitle(),
                ChallengeCategory.valueOf(request.getCategory()),
                request.getSubject(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalParticipants()
        );

        return ChallengeAdminResponseDto.of(challenge, challenge.getCurrentParticipants());
    }

    public void deleteChallenge(Long adminId, Long challengeId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));
        challenge.delete();
    }

    @Transactional(readOnly = true)
    public List<ChallengeAdminResponseDto> getAllChallenges(ChallengeCategory challengeCategory) {
        List<ChallengeAdminResponseDto> response = challengeRepository.getAllChallenges(challengeCategory);
        return response;
    }
}
