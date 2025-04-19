package com.littlebank.finance.domain.user.controller;

import com.littlebank.finance.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-user/user-check")
@RequiredArgsConstructor
@Tag(name = "USER CHECK")
public class UserCheckController {

    private final UserService userService;

    @Operation(summary = "이메일로 userId 조회", description = "해당 이메일을 가진 사용자의 userId 반환")
    @GetMapping("/id-by-email")
    public ResponseEntity<Long> getUserIdByEmail(@RequestParam String email) {
        Long userId = userService.getUserIdByEmail(email);
        return ResponseEntity.ok(userId);
    }
}