CREATE TABLE IF NOT EXISTS review_summary (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    repository      VARCHAR(255)    NOT NULL,
    pr_number       INTEGER         NOT NULL,
    total_issues    INTEGER         NOT NULL DEFAULT 0,
    high_count      INTEGER         NOT NULL DEFAULT 0,
    medium_count    INTEGER         NOT NULL DEFAULT 0,
    low_count       INTEGER         NOT NULL DEFAULT 0,
    review_status   VARCHAR(20)     NOT NULL DEFAULT 'PENDING' CHECK (review_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (repository, pr_number)
);

CREATE INDEX idx_summary_repository   ON review_summary (repository);
CREATE INDEX idx_summary_pr_number    ON review_summary (pr_number);
CREATE INDEX idx_summary_status       ON review_summary (review_status);