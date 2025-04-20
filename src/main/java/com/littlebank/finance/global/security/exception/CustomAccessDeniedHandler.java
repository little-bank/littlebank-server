package com.littlebank.finance.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebank.finance.global.error.dto.ErrorResponse;
import com.littlebank.finance.global.error.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ErrorResponse body = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
