ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS suggestion      TEXT,
    ADD COLUMN IF NOT EXISTS llm_provider    VARCHAR(50),
    ADD COLUMN IF NOT EXISTS confidence_score DECIMAL(3,2);