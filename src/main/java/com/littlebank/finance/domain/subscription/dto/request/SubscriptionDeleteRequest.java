package com.littlebank.finance.domain.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SubscriptionDeleteRequest {
    @Schema(description = "구독ID", example = "1")
    private Long id;
}
