package com.company.dotaadminbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) 
            throws IOException, ServletException {
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        String authHeader = request.getHeader("Authorization");
        String errorMessage;
        String detailMessage;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            errorMessage = "인증이 필요합니다.";
            detailMessage = "Authorization 헤더에 유효한 JWT 토큰을 포함해주세요.";
        } else {
            errorMessage = "토큰이 유효하지 않습니다.";
            detailMessage = "토큰이 만료되었거나 올바르지 않습니다. 다시 로그인해주세요.";
        }
        
        Map<String, Object> errorResponse = Map.of(
            "error", "AUTHENTICATION_FAILED",
            "message", errorMessage,
            "detail", detailMessage,
            "status", 401,
            "path", request.getRequestURI()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}