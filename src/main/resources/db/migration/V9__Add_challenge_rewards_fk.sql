-- Add FK with ON DELETE CASCADE so that deleting a challenge automatically removes related rewards
DO $$
BEGIN
    -- Only add constraint if it does not already exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_challenge_rewards_challenge'
          AND table_name = 'challenge_rewards'
    ) THEN
        ALTER TABLE challenge_rewards
            ADD CONSTRAINT fk_challenge_rewards_challenge
            FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE;
    END IF;
END $$;

-- (Optional) You could also cascade participant/tag rows, but ElementCollection tables are cleaned automatically by Hibernate.