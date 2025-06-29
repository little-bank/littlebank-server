package com.littlebank.finance.domain.challenge.domain.repository.impl;

import com.littlebank.finance.domain.challenge.domain.Challenge;
import com.littlebank.finance.domain.challenge.domain.ChallengeCategory;
import com.littlebank.finance.domain.challenge.domain.QChallenge;
import com.littlebank.finance.domain.challenge.domain.repository.ChallengeRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

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
}
