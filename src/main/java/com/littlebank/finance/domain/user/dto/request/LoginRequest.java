package com.littlebank.finance.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "example@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "비밀번호", example = "password")
    private String password;
}
