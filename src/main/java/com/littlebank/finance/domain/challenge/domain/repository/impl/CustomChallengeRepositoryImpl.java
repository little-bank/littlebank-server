package com.littlebank.finance.domain.challenge.domain.repository.impl;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.QChallenge;
import com.littlebank.finance.domain.challenge.domain.repository.CustomChallengeRepository;
import com.littlebank.finance.domain.challenge.dto.response.admin.ChallengeAdminResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.littlebank.finance.domain.challenge.domain.QChallenge.challenge;

@RequiredArgsConstructor
public class CustomChallengeRepositoryImpl implements CustomChallengeRepository {
    private final JPAQueryFactory queryFactory;
    private QChallenge c = challenge;

    @Override
    public Page<Challenge> findAllByCategory(ChallengeCategory category, Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;

        BooleanBuilder builder = new BooleanBuilder();

        if (category == null || category == ChallengeCategory.ALL) {
            builder.and(challenge.category.in(ChallengeCategory.WEEK, ChallengeCategory.SUBJECT));
        } else {
            builder.and(challenge.category.eq(category));
        }

        List<Challenge> result = queryFactory.selectFrom(challenge)
                .where(builder)
                .orderBy(challenge.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(challenge.count())
                .from(challenge)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public List<ChallengeAdminResponseDto> getAllChallenges(ChallengeCategory challengeCategory) {
        List<ChallengeAdminResponseDto> results =
                queryFactory.select(Projections.constructor(
                        ChallengeAdminResponseDto.class,
                        c.id,
                        c.title,
                        c.category.stringValue(),
                        c.subject,
                        c.startDate,
                        c.endDate,
                        c.currentParticipants,
                        c.totalParticipants,
                        c.viewCount
                ))
                .from(c)
                .fetch();

        return results;
    }
}
