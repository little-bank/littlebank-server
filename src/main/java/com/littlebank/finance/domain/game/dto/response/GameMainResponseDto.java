package com.littlebank.finance.domain.game.dto.response;

import com.littlebank.finance.domain.game.domain.Game;
import com.littlebank.finance.domain.game.domain.GameChoice;
import com.littlebank.finance.domain.game.domain.GameVote;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameMainResponseDto {
    private Long gameId;
    private String question;
    private String option_a;
    private String option_b;
    private boolean voted;

    private GameChoice myChoice;
    private int voteCountA;
    private int voteCountB;
    private double percentA;
    private double percentB;


    public static GameMainResponseDto ofWithResult(Game game, GameVote vote) {
        int a = game.getVote_a();
        int b = game.getVote_b();
        int total = a + b;

        double percentA = total == 0 ? 0 : (double) a / total * 100;
        double percentB = total == 0 ? 0 : (double) b / total * 100;

        return GameMainResponseDto.builder()
                .gameId(game.getId())
                .question(game.getQuestion())
                .option_a(game.getOption_a())
                .option_b(game.getOption_b())
                .voted(true)
                .myChoice(vote.getChoice())
                .voteCountA(a)
                .voteCountB(b)
                .percentA(Math.round(percentA * 10) / 10.0)
                .percentB(Math.round(percentB * 10) / 10.0)
                .build();
    }

    public static GameMainResponseDto ofWithoutResult(Game game) {
        return GameMainResponseDto.builder()
                .gameId(game.getId())
                .question(game.getQuestion())
                .option_a(game.getOption_a())
                .option_b(game.getOption_b())
                .voted(false)
                .build();
    }

    public static GameMainResponseDto ofAdminResult(Game game, GameVote vote) {
        int a = game.getVote_a();
        int b = game.getVote_b();
        int total = a + b;

        double percentA = total == 0 ? 0 : (double) a / total * 100;
        double percentB = total == 0 ? 0 : (double) b / total * 100;

        return GameMainResponseDto.builder()
                .gameId(game.getId())
                .question(game.getQuestion())
                .option_a(game.getOption_a())
                .option_b(game.getOption_b())
                .voted(true)
                .myChoice(null)
                .voteCountA(a)
                .voteCountB(b)
                .percentA(Math.round(percentA * 10) / 10.0)
                .percentB(Math.round(percentB * 10) / 10.0)
                .build();
    }
}
