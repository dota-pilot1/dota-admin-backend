package com.company.dotaadminbackend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 새로운 토큰 생성 메서드 - 사용자 정보와 권한들 모두 포함
    public String generateToken(String email, String roleName, List<String> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", roleName)
                .claim("authorities", authorities)  // 모든 권한 정보를 토큰에 포함
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    // 호환성을 위한 기존 메서드 (권한 정보 없이)
    public String generateToken(String email, String roleName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", roleName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    // 간단한 토큰 생성 메서드 (role 없이 email만)
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return (List<String>) claims.get("authorities");
    }
    
    // 토큰에서 모든 정보를 한번에 추출하는 메서드 (성능 최적화)
    public TokenInfo getTokenInfo(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return new TokenInfo(
            claims.getSubject(), // email
            claims.get("role", String.class), // role
            (List<String>) claims.get("authorities") // authorities
        );
    }
    
    // 토큰 정보를 담는 내부 클래스
    public static class TokenInfo {
        private final String email;
        private final String role;
        private final List<String> authorities;
        
        public TokenInfo(String email, String role, List<String> authorities) {
            this.email = email;
            this.role = role;
            this.authorities = authorities;
        }
        
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public List<String> getAuthorities() { return authorities; }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token expired: " + e.getMessage());
            return false; // 만료는 false 반환하고 별도로 체크
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 이미 만료됨
        } catch (JwtException | IllegalArgumentException e) {
            return true; // 유효하지 않은 토큰은 만료로 간주
        }
    }
}