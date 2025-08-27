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
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
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
                        }
                    } catch (Exception e) {
                        logger.error("Error loading user from database: {}", e.getMessage());
                    }
                } else {
                    logger.warn("JWT token validation failed or expired");
                }
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
            }
        } else {
            logger.debug("No valid Authorization header found");
        }
        
        filterChain.doFilter(request, response);
    }
}