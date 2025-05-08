package com.littlebank.finance.domain.family.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MyFamilyNicknameUpdateRequest {
    @NotNull
    @Schema(description = "가족 구성원 id", example = "1")
    private Long familyMemberId;
    @NotBlank
    @Size(max = 20, message = "별명은 20자 이내여야 합니다")
    @Schema(description = "설정할 별명", example = "나는 zl존 짱")
    private String nickname;
}
