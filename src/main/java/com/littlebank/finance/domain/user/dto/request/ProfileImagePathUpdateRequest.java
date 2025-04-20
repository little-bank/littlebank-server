package com.littlebank.finance.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProfileImagePathUpdateRequest {

    @NotBlank
    @Schema(description = "s3 이미지 업로드 경로 (path)")
    private String profileImagePath;
}
