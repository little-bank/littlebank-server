package com.littlebank.finance.domain.survey.domain.repository;

import com.littlebank.finance.domain.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long>, CustomSurveyRepository {
    List<Survey> findAllByIsDeletedFalse();
}
