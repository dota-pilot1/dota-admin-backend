-- 기존 status check 제약조건을 삭제하고 새로운 enum 값들로 업데이트
ALTER TABLE challenges DROP CONSTRAINT IF EXISTS challenges_status_check;

-- 새로운 status check 제약조건 추가
ALTER TABLE challenges ADD CONSTRAINT challenges_status_check 
    CHECK (status IN ('RECRUITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));

-- 기존 데이터의 status 값들을 새로운 enum 값으로 업데이트
UPDATE challenges 
SET status = CASE 
    WHEN status = 'ACTIVE' THEN 'RECRUITING'
    WHEN status = 'INACTIVE' THEN 'CANCELLED'
    WHEN status = 'COMPLETED' THEN 'COMPLETED'
    ELSE 'RECRUITING'
END;
