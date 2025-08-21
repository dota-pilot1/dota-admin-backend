-- 기존 데이터 삭제 (개발 환경이므로)
DELETE FROM challenge_participants;
DELETE FROM challenge_tags;
DELETE FROM challenges;

-- challenges 테이블 수정
DO $$
BEGIN
    -- author_id 컬럼이 없으면 추가
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='challenges' AND column_name='author_id') THEN
        ALTER TABLE challenges ADD COLUMN author_id BIGINT;
    END IF;
    
    -- author 컬럼이 있으면 삭제
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='challenges' AND column_name='author') THEN
        ALTER TABLE challenges DROP COLUMN author;
    END IF;
    
    -- author_id를 NOT NULL로 설정
    ALTER TABLE challenges ALTER COLUMN author_id SET NOT NULL;
END $$;

-- challenge_participants 테이블 수정
DO $$
BEGIN
    -- participant_id 컬럼이 없으면 추가
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='challenge_participants' AND column_name='participant_id') THEN
        ALTER TABLE challenge_participants ADD COLUMN participant_id BIGINT;
    END IF;
    
    -- participant 컬럼이 있으면 삭제
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='challenge_participants' AND column_name='participant') THEN
        ALTER TABLE challenge_participants DROP COLUMN participant;
    END IF;
END $$;