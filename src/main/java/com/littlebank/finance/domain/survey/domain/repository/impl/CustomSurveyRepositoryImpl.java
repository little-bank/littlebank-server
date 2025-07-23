package com.littlebank.finance.domain.survey.domain.repository.impl;

import com.littlebank.finance.domain.survey.domain.QSurvey;
import com.littlebank.finance.domain.survey.domain.Survey;
import com.littlebank.finance.domain.survey.domain.repository.CustomSurveyRepository;
import com.littlebank.finance.domain.survey.dto.response.CreateSurveyResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.littlebank.finance.domain.survey.domain.QSurvey.survey;

@RequiredArgsConstructor
public class CustomSurveyRepositoryImpl implements CustomSurveyRepository {
    private final JPAQueryFactory queryFactory;
    private final QSurvey s = survey;

    @Override
    public List<CreateSurveyResponseDto> findAllSurveys() {

        List<Survey> surveys = queryFactory
                .selectFrom(s)
                .where(s.isDeleted.eq(false))
                .fetch();

        return surveys.stream()
                .map(CreateSurveyResponseDto::of)
                .toList();
    }

}
