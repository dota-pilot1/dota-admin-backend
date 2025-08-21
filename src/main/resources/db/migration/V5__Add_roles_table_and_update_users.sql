-- V5: Role 테이블 추가 및 User 테이블 업데이트

-- 1. roles 테이블 생성 (이미 존재하지 않는 경우에만)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 2. 기본 role 데이터 삽입 (중복 방지)
INSERT INTO roles (name, description) VALUES 
('USER', '일반 사용자'),
('ADMIN', '관리자'),
('MODERATOR', '중간 관리자'),
('PREMIUM_USER', '프리미엄 사용자')
ON CONFLICT (name) DO NOTHING;

-- 3. users 테이블에 role_id 컬럼 추가 (임시로 nullable, 이미 존재하지 않는 경우에만)
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'role_id') THEN
        ALTER TABLE users ADD COLUMN role_id BIGINT;
    END IF;
END $$;

-- 4. 기존 사용자들의 role_id 설정 (기존 role 컬럼 기반)
UPDATE users SET role_id = (
    CASE 
        WHEN role = 'ADMIN' THEN (SELECT id FROM roles WHERE name = 'ADMIN')
        ELSE (SELECT id FROM roles WHERE name = 'USER')
    END
);

-- 5. role_id를 NOT NULL로 변경하고 외래키 제약 조건 추가
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);

-- 6. 기존 role 컬럼 삭제
ALTER TABLE users DROP COLUMN role;