package com.company.dotaadminbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        String errorMessage = "접근 권한이 없습니다.";
        String detailMessage;
        String suggestion;

        // 현재 인증 정보에서 권한 추출 (익명일 수도 있음)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> currentAuthorities = null;
        if (auth != null) {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            currentAuthorities = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        }

        // 요청 URL에 따른 구체적인 메시지
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/api/admin/roles")) {
            detailMessage = "Role 목록 조회는 ADMIN 전용입니다.";
            suggestion = "관리자 계정으로 로그인하거나, DB에서 해당 사용자 role을 ADMIN 으로 승격 후 새 토큰을 발급받으세요.";
        } else if (requestURI.contains("/api/users")) {
            detailMessage = "이 기능은 관리자만 사용할 수 있습니다.";
            suggestion = "ADMIN 권한이 있는 계정으로 다시 시도하세요.";
        } else if (requestURI.contains("/api/data")) {
            detailMessage = "데이터 관리 기능은 관리자 전용입니다.";
            suggestion = "ADMIN 계정 필요. 초기 관리자 미생성 시 DB에서 role 변경.";
        } else {
            detailMessage = "요청 리소스에 접근 권한이 없습니다.";
            suggestion = "필요한 Role/Authority 확인 후 권한을 부여하세요.";
        }

        // ROLE_ADMIN 누락 여부 힌트
        boolean hasAdmin = currentAuthorities != null
                && currentAuthorities.stream().anyMatch(a -> a.equals("ROLE_ADMIN"));
        String missing = hasAdmin ? null : "ROLE_ADMIN";

        Map<String, Object> errorResponse = Map.of(
                "error", "ACCESS_DENIED",
                "message", errorMessage,
                "detail", detailMessage,
                "suggestion", suggestion,
                "missingRole", missing,
                "currentAuthorities", currentAuthorities,
                "status", 403,
                "path", requestURI);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}