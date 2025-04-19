package com.littlebank.finance.domain.user.dto.request;


import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "example@gmail.com")
    private String email;

    @NotBlank
    @Size(min = 8, message = "패스워드는 8자 이상이여야 합니다.")
    @Schema(description = "비밀번호", example = "password")
    private String password;

    @NotBlank
    @Schema(description = "이름", example = "홍길")
    private String name;

    @Schema(description = "핸드폰 번호", example = "01012345678")
    private String phone;

    @NotBlank
    @Schema(description = "주민등록번호", example = "020625")
    private String rrn;

    @Schema(description = "은행 이름", example = "카카오페이")
    private String bankName;

    @Schema(description = "은행 계좌 번호", example = "0123456789011")
    private String bankAccount;

    @Schema(description = "은행 교유 번호", example = "090")
    private String bankCode;

    @Schema(description = "s3 이미지 업로드 경로")
    private String profileImageUrl;

    @Schema(description = "유저 역할", example = "PARENT or CHILD or TEACHER")
    private UserRole role;

    public User toEntity(Authority authority) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .phone(phone)
                .rrn(rrn)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .bankCode(bankCode)
                .profileImageUrl(bankAccount)
                .role(role)
                .authority(authority)
                .build();
    }
}
