package com.floodrescue.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResult<Void> body = ApiResult.error("Chưa đăng nhập hoặc token không hợp lệ");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}