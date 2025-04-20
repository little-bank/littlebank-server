package com.littlebank.finance.domain.user.dto.request;

import com.littlebank.finance.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserInfoUpdateRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "update@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "이름", example = "장태현")
    private String name;

    @Schema(description = "은행 이름", example = "농협은행")
    private String bankName;

    @Schema(description = "은행 계좌 번호", example = "098765432123")
    private String bankAccount;

    @Schema(description = "은행 교유 번호", example = "011")
    private String bankCode;

    public User toEntity() {
        return User.builder()
                .email(email)
                .name(name)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .bankCode(bankCode)
                .build();
    }
}
