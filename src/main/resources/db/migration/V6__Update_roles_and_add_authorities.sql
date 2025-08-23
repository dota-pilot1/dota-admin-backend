-- V6: 개발 조직용 Role 업데이트 및 Authority 시스템 추가

-- 1. 기존 roles 데이터 삭제 및 개발 조직용 roles 삽입
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;

INSERT INTO roles (name, description) VALUES 
('DEVELOPER', '개발자 - 개발 업무 수행'),
('SENIOR_DEVELOPER', '시니어 개발자 - 코드 리뷰 및 멘토링'),
('TEAM_LEAD', '팀 리더 - 팀 관리 및 업무 배정'),
('PROJECT_MANAGER', '프로젝트 매니저 - 프로젝트 전체 관리'),
('ADMIN', '시스템 관리자 - 전체 시스템 관리');

-- 2. authorities 테이블 생성
CREATE TABLE IF NOT EXISTS authorities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL
);

-- 3. 기능별 Authority 삽입
INSERT INTO authorities (name, description, category) VALUES 
-- 챌린지 관리
('CHALLENGE_CREATE', '챌린지 생성', 'CHALLENGE'),
('CHALLENGE_UPDATE', '챌린지 수정', 'CHALLENGE'),
('CHALLENGE_DELETE', '챌린지 삭제', 'CHALLENGE'),
('CHALLENGE_VIEW_ALL', '모든 챌린지 조회', 'CHALLENGE'),
('CHALLENGE_VIEW_OWN', '본인 챌린지 조회', 'CHALLENGE'),

-- 사용자 관리
('USER_CREATE', '사용자 생성', 'USER'),
('USER_UPDATE', '사용자 정보 수정', 'USER'),
('USER_DELETE', '사용자 삭제', 'USER'),
('USER_VIEW_ALL', '모든 사용자 조회', 'USER'),
('USER_VIEW_TEAM', '팀 사용자 조회', 'USER'),

-- 프로젝트 관리
('PROJECT_CREATE', '프로젝트 생성', 'PROJECT'),
('PROJECT_UPDATE', '프로젝트 수정', 'PROJECT'),
('PROJECT_DELETE', '프로젝트 삭제', 'PROJECT'),
('PROJECT_ASSIGN', '프로젝트 배정', 'PROJECT'),
('PROJECT_VIEW_ALL', '모든 프로젝트 조회', 'PROJECT'),

-- 시스템 관리
('SYSTEM_CONFIG', '시스템 설정 관리', 'SYSTEM'),
('DATA_EXPORT', '데이터 내보내기', 'SYSTEM'),
('LOG_VIEW', '로그 조회', 'SYSTEM'),
('ANALYTICS_VIEW', '분석 데이터 조회', 'SYSTEM');

-- 4. role_authorities 매핑 테이블 생성
CREATE TABLE IF NOT EXISTS role_authorities (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES authorities(id) ON DELETE CASCADE
);

-- 5. user_authorities 개별 권한 테이블 생성
CREATE TABLE IF NOT EXISTS user_authorities (
    user_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    expires_at TIMESTAMP,
    PRIMARY KEY (user_id, authority_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES authorities(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id)
);

-- 6. Role별 기본 Authority 매핑
-- DEVELOPER 권한
INSERT INTO role_authorities (role_id, authority_id) 
SELECT r.id, a.id FROM roles r, authorities a 
WHERE r.name = 'DEVELOPER' AND a.name IN (
    'CHALLENGE_CREATE', 'CHALLENGE_VIEW_OWN', 'CHALLENGE_VIEW_ALL'
);

-- SENIOR_DEVELOPER 권한 (DEVELOPER 권한 + 추가)
INSERT INTO role_authorities (role_id, authority_id) 
SELECT r.id, a.id FROM roles r, authorities a 
WHERE r.name = 'SENIOR_DEVELOPER' AND a.name IN (
    'CHALLENGE_CREATE', 'CHALLENGE_VIEW_OWN', 'CHALLENGE_VIEW_ALL',
    'CHALLENGE_UPDATE', 'USER_VIEW_TEAM'
);

-- TEAM_LEAD 권한 (SENIOR_DEVELOPER 권한 + 추가)
INSERT INTO role_authorities (role_id, authority_id) 
SELECT r.id, a.id FROM roles r, authorities a 
WHERE r.name = 'TEAM_LEAD' AND a.name IN (
    'CHALLENGE_CREATE', 'CHALLENGE_VIEW_OWN', 'CHALLENGE_VIEW_ALL', 'CHALLENGE_UPDATE',
    'USER_VIEW_TEAM', 'USER_VIEW_ALL', 'PROJECT_ASSIGN', 'PROJECT_VIEW_ALL'
);

-- PROJECT_MANAGER 권한 (TEAM_LEAD 권한 + 추가)
INSERT INTO role_authorities (role_id, authority_id) 
SELECT r.id, a.id FROM roles r, authorities a 
WHERE r.name = 'PROJECT_MANAGER' AND a.name IN (
    'CHALLENGE_CREATE', 'CHALLENGE_VIEW_OWN', 'CHALLENGE_VIEW_ALL', 'CHALLENGE_UPDATE', 'CHALLENGE_DELETE',
    'USER_CREATE', 'USER_UPDATE', 'USER_VIEW_ALL', 'USER_VIEW_TEAM',
    'PROJECT_CREATE', 'PROJECT_UPDATE', 'PROJECT_DELETE', 'PROJECT_ASSIGN', 'PROJECT_VIEW_ALL',
    'ANALYTICS_VIEW'
);

-- ADMIN 권한 (모든 권한)
INSERT INTO role_authorities (role_id, authority_id) 
SELECT r.id, a.id FROM roles r, authorities a WHERE r.name = 'ADMIN';

-- 7. 기존 users 데이터 업데이트 (모든 사용자를 DEVELOPER로 설정, admin은 ADMIN으로)
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'DEVELOPER');
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN') 
WHERE username = 'admin' OR email LIKE '%admin%';