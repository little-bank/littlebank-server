package com.littlebank.finance.domain.user.dto.request;


import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.UserConsent;
import com.littlebank.finance.domain.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    @Schema(description = "이름", example = "김동규")
    private String name;
    @NotBlank
    @Schema(description = "핸드폰 번호", example = "01012345678")
    private String phone;
    @NotBlank
    @Schema(description = "생년월일 6자리", example = "020625")
    private String rrn;
    @Schema(description = "은행 이름", example = "카카오페이")
    private String bankName;
    @Schema(description = "은행 교유 번호", example = "090")
    private String bankCode;
    @Schema(description = "은행 계좌 번호", example = "0123456789011")
    private String bankAccount;
    @Schema(description = "계좌 pin번호 6자리", example = "123456")
    private String accountPin;
    @NotNull
    @Schema(description = "유저 역할", example = "PARENT or CHILD or TEACHER")
    private UserRole role;
    @NotNull
    @Schema(description = "수신 동의", example = "true")
    private Boolean isActive;

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
                .accountPin(accountPin)
                .role(role)
                .authority(authority)
                .build();
    }

    public UserConsent toUserConsentEntity(User user) {
        return UserConsent.builder()
                .user(user)
                .isActive(isActive)
                .build();
    }}
