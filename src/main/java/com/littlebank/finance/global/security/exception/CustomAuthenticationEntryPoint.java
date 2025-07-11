package com.littlebank.finance.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebank.finance.global.error.dto.ErrorResponse;
import com.littlebank.finance.global.error.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorResponse body = ErrorResponse.of(ErrorCode.NOT_AUTHENTICATED);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
