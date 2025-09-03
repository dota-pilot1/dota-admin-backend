package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.infrastructure.entity.AuthorityEntity;
import com.company.dotaadminbackend.application.RefreshTokenService;
import com.company.dotaadminbackend.infrastructure.entity.RefreshTokenEntity;
import com.company.dotaadminbackend.infrastructure.entity.UserEntity;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {
    "https://dota-task.shop",
    "https://*.dota-task.shop", 
    "http://localhost:3000",
    "http://localhost:3001",
    "http://localhost:3002",
    "http://localhost:3003"
}, allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("회원가입 요청 - username: {}, email: {}", request.username(), request.email());
        
        try {
            UserEntity user = userService.register(request.username(), request.password(), request.email());
            logger.info("회원가입 성공 - userId: {}, username: {}, email: {}", user.getId(), user.getUsername(), user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "UserEntity registered successfully",
                "userId", user.getId(),
                "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("회원가입 실패 - username: {}, email: {}, error: {}", request.username(), request.email(), e.getMessage());
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/register-with-kakao")
    public ResponseEntity<?> registerWithKakao(@RequestBody RegisterWithKakaoRequest request) {
        try {
            UserEntity user = userService.register(
                request.username(), 
                request.password(), 
                request.email(),
                request.phoneNumber(),
                request.kakaoNotificationConsent()
            );
            return ResponseEntity.ok(Map.of(
                "message", "UserEntity registered successfully with KakaoTalk notification",
                "userId", user.getId(),
                "username", user.getUsername(),
                "phoneNumber", user.getPhoneNumber(),
                "kakaoNotificationConsent", user.isKakaoNotificationConsent()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        logger.info("관리자 생성 요청 - username: {}, email: {}", request.username(), request.email());
        
        try {
            UserEntity user = userService.registerAdmin(request.username(), request.password(), request.email());
            logger.info("관리자 생성 성공 - userId: {}, username: {}, email: {}", user.getId(), user.getUsername(), user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Admin registered successfully",
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().getName()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("관리자 생성 실패 - username: {}, email: {}, error: {}", request.username(), request.email(), e.getMessage());
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, @RequestHeader(value = "User-Agent", required = false) String userAgent, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        logger.info("로그인 시도 - email: {}, IP: {}, User-Agent: {}", request.email(), httpRequest.getRemoteAddr(), userAgent);
        
        Optional<UserEntity> userOpt = userService.findByEmail(request.email());
        
        if (userOpt.isEmpty()) {
            logger.warn("로그인 실패 - 사용자 없음: {}", request.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        UserEntity user = userOpt.get();
        if (!userService.validatePassword(request.password(), user.getPassword())) {
            logger.warn("로그인 실패 - 비밀번호 틀림: {}", request.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        logger.info("로그인 성공 - 사용자: {}", user.getEmail());

        // 🚀 사용자의 모든 권한 조회 (로그인 시 1회만)
        List<AuthorityEntity> userAuthorities = userService.getUserAuthorities(user.getId());
        List<String> authorityNames = userAuthorities.stream()
                .map(AuthorityEntity::getName)
                .toList();

        // 🎯 권한 정보를 포함한 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName(), authorityNames);
    RefreshTokenService.GeneratedToken refresh = refreshTokenService.create(user, httpRequest.getRemoteAddr(), userAgent);
    
    // 로그 추가
    logger.info("Setting refresh token cookie for user: {}", user.getEmail());
    
    Cookie cookie = new Cookie("refresh_token", refresh.raw);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // TODO: prod 환경에서 true (HTTPS)
    cookie.setPath("/");
    cookie.setMaxAge(14 * 24 * 3600);
    
    // 쿠키 추가 (기본 방식으로 먼저 시도)
    httpResponse.addCookie(cookie);
    
    // 추가적으로 Set-Cookie 헤더도 설정 (SameSite 포함)
    String cookieHeader = String.format(
        "%s=%s; Path=%s; Max-Age=%d; HttpOnly; SameSite=Lax", 
        cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getMaxAge()
    );
    httpResponse.addHeader("Set-Cookie", cookieHeader);
    
    logger.info("Refresh token cookie set successfully with header: {}", 
        cookieHeader.substring(0, Math.min(50, cookieHeader.length())) + "...");;

        logger.info("로그인 응답 전송 - 토큰 길이: {}, 권한: {}", token.length(), authorityNames);
        
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token,
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().getName(),
            "authorities", authorityNames,
            "expiresIn", 300
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response, @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        logger.info("토큰 갱신 요청 - IP: {}, User-Agent: {}", request.getRemoteAddr(), userAgent);
        
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.warn("토큰 갱신 실패 - 쿠키 없음");
            return ResponseEntity.status(401).body(Map.of("error", "NO_REFRESH_COOKIE"));
        }
        
        logger.info("전체 쿠키 개수: {}", cookies.length);
        for (Cookie c : cookies) {
            logger.info("쿠키: {} = {}", c.getName(), c.getValue().substring(0, Math.min(10, c.getValue().length())) + "...");
        }
        
        String raw = null;
        for (Cookie c : cookies) {
            if ("refresh_token".equals(c.getName())) {
                raw = c.getValue();
                logger.info("refresh_token 쿠키 발견: {}...", raw.substring(0, Math.min(10, raw.length())));
            }
        }
        if (raw == null) {
            logger.warn("토큰 갱신 실패 - refresh_token 쿠키 없음");
            return ResponseEntity.status(401).body(Map.of("error", "NO_REFRESH_COOKIE"));
        }
        
    java.util.Optional<RefreshTokenEntity> validOpt = refreshTokenService.findValid(raw);
        if (validOpt.isEmpty()) {
            logger.warn("토큰 갱신 실패 - 유효하지 않은 토큰");
            return ResponseEntity.status(401).body(Map.of("error", "INVALID_REFRESH"));
        }
        RefreshTokenEntity oldToken = validOpt.get();
        if (oldToken.isRevoked()) {
            logger.warn("토큰 갱신 실패 - 이미 무효화된 토큰");
            return ResponseEntity.status(401).body(Map.of("error", "REVOKED"));
        }
        
        logger.info("토큰 갱신 성공 - 사용자: {}", oldToken.getUser().getEmail());
        
        // rotate
        refreshTokenService.revoke(oldToken);
    RefreshTokenService.GeneratedToken rotated = refreshTokenService.create(oldToken.getUser(), request.getRemoteAddr(), userAgent);
        
        logger.info("Setting rotated refresh token cookie for user: {}", oldToken.getUser().getEmail());
        
        Cookie cookie = new Cookie("refresh_token", rotated.raw);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // prod: true
        cookie.setPath("/");
        cookie.setMaxAge(14 * 24 * 3600);
        
        // 쿠키 추가 (기본 방식으로 먼저 시도)
        response.addCookie(cookie);
        
        // 추가적으로 Set-Cookie 헤더도 설정 (SameSite 포함)
        String cookieHeader = String.format(
            "%s=%s; Path=%s; Max-Age=%d; HttpOnly; SameSite=Lax", 
            cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getMaxAge()
        );
        response.addHeader("Set-Cookie", cookieHeader);
        
        // 🚀 사용자의 모든 권한 조회 (refresh 시에도 최신 권한 반영)
        List<AuthorityEntity> userAuthorities = userService.getUserAuthorities(oldToken.getUser().getId());
        List<String> authorityNames = userAuthorities.stream()
                .map(AuthorityEntity::getName)
                .toList();
        
        String access = jwtUtil.generateToken(oldToken.getUser().getEmail(), oldToken.getUser().getRole().getName(), authorityNames);
        logger.info("Refresh token rotated successfully with header: {}",
            cookieHeader.substring(0, Math.min(50, cookieHeader.length())) + "...");
        return ResponseEntity.ok(Map.of("accessToken", access, "expiresIn", 300));
    }

    public static class RegisterRequest {
        private String username; private String password; private String email;
        public String username() { return username; }
        public String password() { return password; }
        public String email() { return email; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setEmail(String email) { this.email = email; }
    }
    public static class RegisterWithKakaoRequest {
        private String username; private String password; private String email; private String phoneNumber; private boolean kakaoNotificationConsent;
        public String username() { return username; }
        public String password() { return password; }
        public String email() { return email; }
        public String phoneNumber() { return phoneNumber; }
        public boolean kakaoNotificationConsent() { return kakaoNotificationConsent; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setEmail(String email) { this.email = email; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public void setKakaoNotificationConsent(boolean kakaoNotificationConsent) { this.kakaoNotificationConsent = kakaoNotificationConsent; }
    }
    public static class LoginRequest {
        private String email; private String password;
        public String email() { return email; }
        public String password() { return password; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }
}