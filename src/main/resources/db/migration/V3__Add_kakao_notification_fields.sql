-- Add phone number and kakao notification consent fields to users table
ALTER TABLE users 
ADD COLUMN phone_number VARCHAR(20) UNIQUE,
ADD COLUMN kakao_notification_consent BOOLEAN NOT NULL DEFAULT FALSE;