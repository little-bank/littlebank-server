package com.littlebank.finance.domain.game.service.admin;

import com.littlebank.finance.domain.game.domain.Game;
import com.littlebank.finance.domain.game.domain.repository.GameRepository;
import com.littlebank.finance.domain.game.dto.request.GameRequestDto;
import com.littlebank.finance.domain.game.dto.response.GameResponseDto;
import com.littlebank.finance.domain.game.exception.GameException;
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
public class AdminGameService {
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public GameResponseDto createGame(Long adminId, GameRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Game game = Game.builder()
                .question(request.getQuestion())
                .option_a(request.getOption_a())
                .option_b(request.getOption_b())
                .vote_a(0)
                .vote_b(0)
                .build();
        Game savedGame = gameRepository.save(game);

        return GameResponseDto.of(savedGame);
    }

    public GameResponseDto updateGame(Long adminId, Long gameId, GameRequestDto request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameException(ErrorCode.GAME_NOT_FOUND));

        game.update(request.getQuestion(), request.getOption_a(), request.getOption_b(), game.getVote_a(), game.getVote_b());

        return GameResponseDto.of(game);
    }

    public void deleteGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameException(ErrorCode.GAME_NOT_FOUND));
        gameRepository.deleteById(game.getId());
    }
}
