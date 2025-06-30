package com.littlebank.finance.domain.point.dto.request;

import com.littlebank.finance.domain.point.domain.RewardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PointTransferRequest {
    @NotNull
    @Schema(description = "포인트를 보낼 유저")
    private Long receiverId;
    @NotNull
    @Schema(description = "이체할 포인트 양")
    private Integer pointAmount;
    @NotNull
    @Schema(description = "이체 메시지(메시지가 없다면 공백)")
    private String message;
    @NotNull
    @Schema(description = "미션/챌린지/목표/일반 중 하나")
    private RewardType rewardType;
    @Schema(description = "포인트를 지급한 미션/챌린지/목표의 id")
    private Long rewardId;
}
