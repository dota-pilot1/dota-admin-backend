package com.company.dotaadminbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.infrastructure.entity.AuthorityEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final ApplicationContext applicationContext;
    private UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ApplicationContext applicationContext) {
        this.jwtUtil = jwtUtil;
        this.applicationContext = applicationContext;
    }
    
    private UserService getUserService() {
        if (userService == null) {
            userService = applicationContext.getBean(UserService.class);
        }
        return userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
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
                    // 일반 401 - refresh 시도 안함
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                String email = jwtUtil.getEmailFromToken(token);
                logger.debug("JWT parsed successfully - email: {}", email);
                
                try {
                    UserService userSvc = getUserService();
                    UserEntity user = userSvc.findByEmail(email).orElse(null);
                    
                    if (user != null) {
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        
                        // DB에서 가져온 실제 Role 사용
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
                        
                        // AuthorityEntity 기반 권한 추가 (DB에서 실시간 조회)
                        List<AuthorityEntity> userAuthorities = userSvc.getUserAuthorities(user.getId());
                        for (AuthorityEntity authority : userAuthorities) {
                            authorities.add(new SimpleGrantedAuthority(authority.getName()));
                        }
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, null, authorities
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Authentication set in SecurityContext for user: {} with role: {} and {} authorities", 
                                   email, user.getRole().getName(), authorities.size());
                    } else {
                        logger.warn("UserEntity not found in database: {}", email);
                        // 일반 401 - refresh 시도 안함
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Error loading user from database: {}", e.getMessage());
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