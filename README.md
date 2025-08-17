# Dota Admin Backend

Spring Boot 3.5.x + Java 21 backend. Simple MVC stack with JPA, Flyway, PostgreSQL (Docker).

## Tech stack (dev spec)
- Java 21, Gradle 8
- Spring Boot: Web (MVC), Security (dev-permit), Data JPA, Validation, GraphQL (placeholder), jOOQ (placeholder)
- DB: PostgreSQL (Docker), Flyway migrations, H2 for local fallback (optional)
- Tools: IntelliJ IDEA or VS Code, Docker Desktop, Postman

## Prerequisites
- Windows 10/11
- Java 21 (Temurin/Adoptium 권장)
- Docker Desktop (WSL2 backend 권장)

## Project layout
- `src/main/java/.../web` — REST controllers
- `src/main/java/.../application` — services
- `src/main/java/.../infrastructure` — JPA entities and Spring Data repositories
- `src/main/resources/application.yml` — environment config
- `docker.yml` — Postgres 15 compose file

## 1) Database (Docker)
```powershell
# 컨테이너 실행
docker compose -f .\docker.yml up -d

# 상태 확인
docker ps --filter "name=dota-admin-postgres"

# (선택) DB/테이블 확인
docker exec -it dota-admin-postgres psql -U postgres -d dota_admin -c "\\dt"
```
Compose 설정:
- DB: `dota_admin`
- User/Pass: `postgres` / `postgres`
- Port: `5432`

## 2) Application config
기본 설정은 `src/main/resources/application.yml`:
```
spring:
	datasource:
		url: jdbc:postgresql://localhost:5432/dota_admin
		username: postgres
		password: postgres
	jpa:
		hibernate:
			ddl-auto: update
```
프로파일/환경변수로 SPRING_DATASOURCE_* 오버라이드 가능.

## 3) Run the app
- IntelliJ: `DotaAdminBackendApplication` 실행
- Gradle (PowerShell):
```powershell
./gradlew.bat bootRun
```

## 4) Quick API check
- GET `http://localhost:8080/api/players/hello` -> `Hello from backend`
- POST `http://localhost:8080/api/players?name=Alice` -> created player JSON

## 5) Security/CORS (dev)
- `SecurityConfig`에서 모든 요청을 허용하고 CSRF 비활성화
- CORS: `http://localhost:3000` 허용(Next.js 개발 서버용)
> 운영 전환 시 JWT/세션 기반 인증과 접근 제어로 변경 필요

## 6) Migrations (Flyway)
- 경로: `src/main/resources/db/migration`
- 예시 파일명: `V1__init.sql`

## 7) Build & Test
```powershell
./gradlew.bat clean build -x test   # 빠른 빌드
./gradlew.bat test                  # 테스트 실행
```

## 8) Troubleshooting
- 401 Unauthorized: 개발용 `SecurityConfig` 반영됐는지 확인(서버 재시작)
- DB 연결 오류: 도커 컨테이너 상태/포트, `application.yml`의 URL/계정 확인
- 포트 충돌: 로컬 5432/8080 점유 확인 (`netstat -ano | findstr 5432`)

## 9) Git/GitHub (초기 커밋)
```powershell
git init
git checkout -b main
git add .
git commit -m "chore: init backend (Spring MVC + JPA + Docker Postgres + dev security)"
# GitHub에서 새 저장소 생성 후
git remote add origin https://github.com/<OWNER>/<REPO>.git
git push -u origin main
```

## Next steps
- 로그인/회원가입(JWT) 추가: User 엔티티/Flyway, BCrypt, Auth API, JWT 필터, 보안 정책 조정
- DTO/매퍼 도입: 엔티티 직접 노출 방지
- 통합 테스트(Testcontainers) 작성
