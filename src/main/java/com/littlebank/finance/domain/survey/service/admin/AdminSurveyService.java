package com.littlebank.finance.domain.survey.service.admin;


import com.littlebank.finance.domain.survey.domain.Survey;
import com.littlebank.finance.domain.survey.domain.repository.SurveyRepository;
import com.littlebank.finance.domain.survey.dto.request.CreateSurveyRequestDto;
import com.littlebank.finance.domain.survey.dto.response.CreateSurveyResponseDto;
import com.littlebank.finance.domain.survey.exception.SurveyException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminSurveyService {
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    public CreateSurveyResponseDto createSurvey(Long adminId, CreateSurveyRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Survey survey = surveyRepository.save(request.toSurvey());
        return CreateSurveyResponseDto.of(survey);
    }

    public CreateSurveyResponseDto updateSurvey(Long adminId, Long surveyId, CreateSurveyRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException(ErrorCode.SURVEY_NOT_FOUND));
        survey.update(request.getQuestion(), request.getOptionA(), request.getOptionB(), request.getOptionC());
        return CreateSurveyResponseDto.of(survey);
    }

    public void deleteSurvey(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException(ErrorCode.SURVEY_NOT_FOUND));
    surveyRepository.deleteById(survey.getId());
    }
}
