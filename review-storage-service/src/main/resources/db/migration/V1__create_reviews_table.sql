CREATE TABLE IF NOT EXISTS reviews (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    repository      VARCHAR(255)    NOT NULL,
    pr_number       INTEGER         NOT NULL,
    file_name       VARCHAR(500)    NOT NULL,
    comment         TEXT            NOT NULL,
    severity        VARCHAR(20)     NOT NULL CHECK (severity IN ('HIGH', 'MEDIUM', 'LOW')),
    category        VARCHAR(50),
    line_number     INTEGER,
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED', 'IGNORED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_repository   ON reviews (repository);
CREATE INDEX idx_reviews_pr_number    ON reviews (pr_number);
CREATE INDEX idx_reviews_severity     ON reviews (severity);
CREATE INDEX idx_reviews_repo_pr      ON reviews (repository, pr_number);