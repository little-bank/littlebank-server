package com.littlebank.finance.domain.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AmountTempSaveRequest {
    @NotNull
    @Schema(description = "UUID 값", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private String orderId;
    @NotNull
    @Schema(description = "결제 금액", example = "3000")
    private Integer amount;
}
