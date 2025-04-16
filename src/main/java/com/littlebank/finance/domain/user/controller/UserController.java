package com.littlebank.finance.domain.user.controller;

import com.littlebank.finance.domain.user.domain.Authority;
import com.littlebank.finance.domain.user.dto.request.SignupRequest;
import com.littlebank.finance.domain.user.dto.response.SignupResponse;
import com.littlebank.finance.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-user/user")
@RequiredArgsConstructor
@Tag(name = "USER", description = "회원 관련 API")
public class UserController {
    private final UserService userService;

    @PostMapping("/public/signup")
    @SecurityRequirements()
    @Operation(summary = "회원가입 API", description = "설명")
    public ResponseEntity<SignupResponse> saveUser(
            @RequestBody @Valid SignupRequest request
    ) {
        SignupResponse response = userService.saveUser(request.toEntity(Authority.USER));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
