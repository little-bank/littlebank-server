package com.littlebank.finance.domain.survey.domain.repository;

import com.littlebank.finance.domain.survey.dto.response.CreateSurveyResponseDto;

import java.util.List;

public interface CustomSurveyRepository {
    List<CreateSurveyResponseDto> findAllSurveys();
}
