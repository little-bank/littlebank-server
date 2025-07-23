package com.littlebank.finance.domain.game.domain.repository;

import com.littlebank.finance.domain.game.dto.response.GameMainResponseDto;

import java.util.List;

public interface CustomGameRepository {
    List<GameMainResponseDto> findAllByUserVote();
}
