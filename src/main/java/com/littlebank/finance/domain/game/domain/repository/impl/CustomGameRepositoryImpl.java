package com.littlebank.finance.domain.game.domain.repository.impl;

import com.littlebank.finance.domain.game.domain.Game;
import com.littlebank.finance.domain.game.domain.QGame;
import com.littlebank.finance.domain.game.domain.repository.CustomGameRepository;
import com.littlebank.finance.domain.game.dto.response.GameMainResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.littlebank.finance.domain.game.domain.QGame.game;

@RequiredArgsConstructor
public class CustomGameRepositoryImpl implements CustomGameRepository {
    private final JPAQueryFactory queryFactory;
    private QGame g = game;

    @Override
    public List<GameMainResponseDto> findAllByUserVote() {
        List<Game> games = queryFactory
                .selectFrom(g)
                .where(g.isDeleted.eq(false))
                .fetch();

        return games.stream()
                .map(game -> GameMainResponseDto.ofAdminResult(game, null))
                .toList();
    }
}
