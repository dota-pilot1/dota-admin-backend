package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.domain.model.User;
import com.company.dotaadminbackend.application.UserService;
import com.company.dotaadminbackend.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.email());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!userService.validatePassword(request.password(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token,
            "userId", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterWithKakaoRequest(String username, String password, String email, String phoneNumber, boolean kakaoNotificationConsent) {}
    public record LoginRequest(String email, String password) {}
}