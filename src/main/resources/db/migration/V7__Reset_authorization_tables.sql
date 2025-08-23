-- V7: Authorization reset (temporary open access)
-- Purpose: Clear roles/authorities mappings so backend runs without legacy constraints while redesigning permission model.

-- 1. Remove role-authority relationships and user-specific authorities if tables exist
DO $$
BEGIN
    IF to_regclass('public.role_authorities') IS NOT NULL THEN
        TRUNCATE TABLE role_authorities RESTART IDENTITY CASCADE;
    END IF;
    IF to_regclass('public.user_authorities') IS NOT NULL THEN
        TRUNCATE TABLE user_authorities RESTART IDENTITY CASCADE;
    END IF;
    IF to_regclass('public.authorities') IS NOT NULL THEN
        TRUNCATE TABLE authorities RESTART IDENTITY CASCADE;
    END IF;
    -- Keep roles table but simplify to just USER and ADMIN for now.
    IF to_regclass('public.roles') IS NOT NULL THEN
        TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
        INSERT INTO roles (name, description) VALUES
            ('USER', '기본 사용자 (권한 재설계 전 임시)'),
            ('ADMIN', '관리자 (권한 재설계 전 임시)');
    END IF;
END $$;

-- 2. Update existing users: set all to USER except ones containing 'admin'
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'USER');
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE username ILIKE '%admin%' OR email ILIKE '%admin%';

-- NOTE: After redesign, introduce new stable permission codes and mapping migrations.
