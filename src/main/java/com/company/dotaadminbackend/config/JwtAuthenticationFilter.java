package com.company.dotaadminbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.dotaadminbackend.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // WebSocket 업그레이드 요청은 JWT 필터 건너뛰기
        String uri = request.getRequestURI();
        String upgrade = request.getHeader("Upgrade");
        if ((uri.startsWith("/ws") || uri.startsWith("/ws-sockjs")) && "websocket".equalsIgnoreCase(upgrade)) {
            logger.debug("Skipping JWT filter for WebSocket upgrade request: {}", uri);
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            
            try {
                // 토큰 만료 체크가 우선
                if (jwtUtil.isTokenExpired(token)) {
                    logger.warn("JWT token expired");
                    sendTokenErrorResponse(response, "TOKEN_EXPIRED", "Token expired");
                    return;
                }
                
                // 토큰 유효성 체크
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("JWT token invalid");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                // 🚀 성능 최적화: 토큰에서 모든 정보를 한번에 추출 (DB 조회 없음!)
                try {
                    JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
                    String email = tokenInfo.getEmail();
                    String role = tokenInfo.getRole();
                    List<String> authorities = tokenInfo.getAuthorities();
                    
                    logger.debug("JWT parsed successfully - email: {}, role: {}", email, role);
                    
                    // Spring Security 권한 목록 생성
                    List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
                    
                    // Role 권한 추가 (ROLE_ 접두사 포함)
                    if (role != null && !role.isEmpty()) {
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    
                    // Authority 권한들 추가 (토큰에서 가져온 것)
                    if (authorities != null && !authorities.isEmpty()) {
                        for (String authority : authorities) {
                            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
                        }
                    }
                    
                    // SecurityContext에 인증 정보 설정
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, grantedAuthorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set from token for user: {} with role: {} and {} authorities", 
                               email, role, grantedAuthorities.size());
                    
                } catch (Exception e) {
                    logger.error("Error parsing JWT token: {}", e.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            logger.debug("No valid Authorization header found");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void sendTokenErrorResponse(HttpServletResponse response, String errorCode, String message) 
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ErrorResponse errorResponse = new ErrorResponse(message, errorCode);
        ObjectMapper objectMapper = new ObjectMapper();
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}