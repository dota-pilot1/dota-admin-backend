package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final SimplePresenceService presenceService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getPresence() {
        return ResponseEntity.ok(Map.of("online", presenceService.getOnlineUsers()));
    }
    
    // 테스트용: 수동으로 사용자 등록
    @PostMapping("/test-login")
    public ResponseEntity<?> testLogin(@RequestParam String userId) {
        String sessionId = "test-session-" + System.currentTimeMillis();
        presenceService.onConnect(sessionId, userId);
        return ResponseEntity.ok(Map.of(
            "message", "User " + userId + " added", 
            "online", presenceService.getOnlineUsers()
        ));
    }
    
    // 로그인 시 자동 등록용
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestHeader("Authorization") String authHeader) {
        try {
            // Bearer 토큰에서 사용자 정보 추출 
            String token = authHeader.replace("Bearer ", "");
            
            // JWT에서 실제 사용자 정보 추출
            JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
            String userId = tokenInfo.getEmail(); // 이메일을 userId로 사용
            String sessionId = "session-" + userId + "-" + System.currentTimeMillis();
            
            presenceService.onConnect(sessionId, userId);
            return ResponseEntity.ok(Map.of(
                "message", "Connected successfully", 
                "userId", userId,
                "online", presenceService.getOnlineUsers()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 테스트용: 모든 사용자 정리
    @PostMapping("/clear")
    public ResponseEntity<?> clearAll() {
        presenceService.clearAll();
        return ResponseEntity.ok(Map.of(
            "message", "All users cleared",
            "online", presenceService.getOnlineUsers()
        ));
    }
}
