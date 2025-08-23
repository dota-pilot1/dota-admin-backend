package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.domain.model.Authority;
import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("회원가입 요청 - username: {}, email: {}", request.username(), request.email());
        
        try {
            User user = userService.register(request.username(), request.password(), request.email());
            logger.info("회원가입 성공 - userId: {}, username: {}, email: {}", user.getId(), user.getUsername(), user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User registered successfully",
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
            User user = userService.register(
                request.username(), 
                request.password(), 
                request.email(),
                request.phoneNumber(),
                request.kakaoNotificationConsent()
            );
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully with KakaoTalk notification",
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
            User user = userService.registerAdmin(request.username(), request.password(), request.email());
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.email());
        
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!userService.validatePassword(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // 사용자의 모든 권한 조회
        List<Authority> userAuthorities = userService.getUserAuthorities(user.getId());
        List<String> authorityNames = userAuthorities.stream()
                .map(Authority::getName)
                .toList();

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());

        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token,
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().getName(),
            "authorities", authorityNames
        ));
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterWithKakaoRequest(String username, String password, String email, String phoneNumber, boolean kakaoNotificationConsent) {}
    public record LoginRequest(String email, String password) {}
}