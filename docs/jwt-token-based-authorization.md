# JWT 토큰 기반 권한 관리 시스템

## 1. 개요

기존 DB 조회 방식에서 **JWT 토큰에 권한 정보를 직접 포함**하는 방식으로 리팩토링하여 성능을 대폭 개선한 권한 관리 시스템입니다.

### 기존 방식 vs 개선된 방식

| 구분 | 기존 방식 | 개선된 방식 |
|------|-----------|-------------|
| 권한 정보 저장 | DB에만 저장 | JWT 토큰에 포함 |
| 매 API 요청 시 | User + Role + Authority DB 조회 | 토큰 파싱만으로 권한 추출 |
| 성능 | DB I/O 부하 | 메모리 기반 처리 |
| 확장성 | DB 부하로 제한적 | 높은 처리량 가능 |

## 2. 권한 관리 구조

```
UserEntity (사용자)
    ↓ (1:1 관계)
RoleEntity (역할)
    ↓ (1:N 관계)  
AuthorityEntity (권한)
```

### 데이터베이스 구조
- **users**: 사용자 기본 정보 + role_id 참조
- **roles**: 역할 정보 (ADMIN, USER 등)
- **authorities**: 세부 권한 (CREATE_USER, DELETE_POST 등)
- **role_authorities**: 역할-권한 매핑 테이블

## 3. JWT 토큰 구조

### 기존 토큰 구조
```json
{
  "sub": "user@example.com",
  "role": "ADMIN",
  "iat": 1693123456,
  "exp": 1693127056
}
```

### 개선된 토큰 구조
```json
{
  "sub": "user@example.com", 
  "role": "ADMIN",
  "authorities": [
    "CREATE_USER",
    "DELETE_USER", 
    "VIEW_ADMIN_PANEL",
    "MANAGE_ROLES"
  ],
  "iat": 1693123456,
  "exp": 1693127056
}
```

## 4. Step-by-Step 권한 인증 프로세스

### Step 1: 로그인 시 토큰 생성
```java
// AuthController.java:137-144
// 🚀 사용자의 모든 권한 조회 (로그인 시 1회만)
List<AuthorityEntity> userAuthorities = userService.getUserAuthorities(user.getId());
List<String> authorityNames = userAuthorities.stream()
        .map(AuthorityEntity::getName)
        .toList();

// 🎯 권한 정보를 포함한 토큰 생성
String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName(), authorityNames);
```

### Step 2: JWT 토큰 파싱 및 인증 정보 추출
```java
// JwtAuthenticationFilter.java:58-65
// 🚀 성능 최적화: 토큰에서 모든 정보를 한번에 추출 (DB 조회 없음!)
JwtUtil.TokenInfo tokenInfo = jwtUtil.getTokenInfo(token);
String email = tokenInfo.getEmail();
String role = tokenInfo.getRole();
List<String> authorities = tokenInfo.getAuthorities();
```

### Step 3: Spring Security 권한 객체 생성
```java
// JwtAuthenticationFilter.java:67-80
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
```

### Step 4: SecurityContext 설정
```java
// JwtAuthenticationFilter.java:82-87
UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    email, null, grantedAuthorities
);

SecurityContextHolder.getContext().setAuthentication(authToken);
```

### Step 5: API 권한 검증
컨트롤러에서 `@PreAuthorize` 어노테이션으로 권한 검증:
```java
@PreAuthorize("hasAuthority('CREATE_USER')")
@PostMapping("/users")
public ResponseEntity<?> createUser() {
    // 권한이 있는 경우에만 실행
}

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/dashboard")
public ResponseEntity<?> getAdminDashboard() {
    // ADMIN 역할을 가진 사용자만 접근 가능
}
```

## 5. 토큰 갱신 시 권한 업데이트

### Refresh Token 처리
```java
// AuthController.java:245-251
// 🚀 사용자의 모든 권한 조회 (refresh 시에도 최신 권한 반영)
List<AuthorityEntity> userAuthorities = userService.getUserAuthorities(oldToken.getUser().getId());
List<String> authorityNames = userAuthorities.stream()
        .map(AuthorityEntity::getName)
        .toList();

String access = jwtUtil.generateToken(oldToken.getUser().getEmail(), oldToken.getUser().getRole().getName(), authorityNames);
```

## 6. 성능 개선 효과

### 기존 방식 (매 API 요청마다)
1. JWT 토큰에서 email 추출
2. DB에서 UserEntity 조회 
3. DB에서 RoleEntity 조회
4. DB에서 AuthorityEntity 목록 조회
5. Spring Security 객체 생성

**총 DB 쿼리: 3~4회**

### 개선된 방식 (매 API 요청마다)
1. JWT 토큰에서 email, role, authorities 한번에 추출
2. Spring Security 객체 생성

**총 DB 쿼리: 0회**

### 예상 성능 개선
- **응답 속도**: 50-80% 향상 (DB I/O 제거)
- **처리량**: 3-5배 증가
- **DB 부하**: 대폭 감소

## 7. 코드 변경 사항 요약

### 수정된 파일들
1. **JwtUtil.java** 
   - `generateToken(email, role, authorities)` 메서드 추가
   - `getTokenInfo()` 성능 최적화 메서드 추가
   - `TokenInfo` 내부 클래스 추가

2. **JwtAuthenticationFilter.java**
   - DB 조회 코드 완전 제거
   - 토큰에서 직접 권한 정보 추출하도록 변경

3. **AuthController.java** 
   - 로그인 시 사용자 권한 조회하여 토큰에 포함
   - 토큰 갱신 시에도 최신 권한 정보 반영

## 8. 장점과 단점

### 장점
✅ **높은 성능**: DB 조회 없이 메모리에서 권한 처리  
✅ **확장성**: 높은 동시 접속자 처리 가능  
✅ **단순함**: 복잡한 캐싱 로직 불필요  
✅ **일관성**: 토큰 유효 기간 동안 권한 정보 일관성 보장  

### 단점 (및 해결책)
⚠️ **권한 변경 시 즉시 반영 안됨**  
→ 해결책: 토큰 재발급 또는 짧은 만료시간 설정

⚠️ **토큰 크기 증가**  
→ 해결책: 권한명 최적화, 압축 등

## 9. 보안 고려사항

1. **토큰 만료 시간**: 권한 변경의 즉시성과 성능 사이의 균형점 설정
2. **토큰 탈취 방지**: HTTPS 사용, HttpOnly 쿠키 등
3. **권한 최소화**: 필요한 최소한의 권한만 부여

## 10. 결론

JWT 토큰 기반 권한 관리 시스템으로 리팩토링하여:
- **성능 대폭 개선** (DB I/O 제거)
- **시스템 확장성 증대** 
- **코드 복잡도 감소**

이제 높은 트래픽 환경에서도 안정적이고 빠른 권한 관리가 가능합니다.