package com.company.dotaadminbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) 
            throws IOException, ServletException {
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorMessage = "접근 권한이 없습니다.";
        String detailMessage = "";
        
        // 요청 URL에 따른 구체적인 메시지
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/api/users")) {
            detailMessage = "이 기능은 관리자만 사용할 수 있습니다. 관리자 권한이 필요합니다.";
        } else if (requestURI.contains("/api/data")) {
            detailMessage = "데이터 관리 기능은 관리자 전용입니다.";
        } else {
            detailMessage = "요청하신 리소스에 접근할 권한이 없습니다.";
        }
        
        Map<String, Object> errorResponse = Map.of(
            "error", "ACCESS_DENIED",
            "message", errorMessage,
            "detail", detailMessage,
            "status", 403,
            "path", requestURI
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}