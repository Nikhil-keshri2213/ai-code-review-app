package com.aicodereview.aireview.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import com.aicodereview.review.service.SeverityScorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityScorerTest {

    private SeverityScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new SeverityScorer();
    }

    private ReviewResult buildResult(Severity severity,
                                      ReviewCategory category,
                                      String comment) {
        return ReviewResult.builder()
                .resultId(UUID.randomUUID())
                .fileName("Test.java")
                .severity(severity)
                .category(category)
                .comment(comment)
                .suggestion("Fix it")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void score_securityCategory_alwaysHigh() {
        ReviewResult result = buildResult(
                Severity.LOW, ReviewCategory.SECURITY, "Auth issue");

        ReviewResult scored = scorer.score(result);

        assertThat(scored.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(scored.getConfidenceScore()).isEqualTo(0.6);
    }

    @Test
    void score_sqlInjectionKeyword_forcesHigh() {
        ReviewResult result = buildResult(
                Severity.MEDIUM, ReviewCategory.BUG,
                "SQL injection vulnerability found in query");

        ReviewResult scored = scorer.score(result);

        assertThat(scored.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(scored.getCategory()).isEqualTo(ReviewCategory.SECURITY);
        assertThat(scored.getConfidenceScore()).isEqualTo(0.6);
    }

    @Test
    void score_hardcodedPasswordKeyword_forcesHigh() {
        ReviewResult result = buildResult(
                Severity.LOW, ReviewCategory.CODE_STYLE,
                "Hardcoded password detected in source");

        ReviewResult scored = scorer.score(result);

        assertThat(scored.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(scored.getConfidenceScore()).isEqualTo(0.6);
    }

    @Test
    void score_codeStyleLow_stays_low() {
        ReviewResult result = buildResult(
                Severity.LOW, ReviewCategory.CODE_STYLE,
                "Variable name could be more descriptive");

        ReviewResult scored = scorer.score(result);

        assertThat(scored.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(scored.getConfidenceScore()).isEqualTo(1.0);
    }

    @Test
    void score_highSecurity_noOverride_confidence1() {
        ReviewResult result = buildResult(
                Severity.HIGH, ReviewCategory.SECURITY,
                "SQL injection in query builder");

        ReviewResult scored = scorer.score(result);

        assertThat(scored.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(scored.getConfidenceScore()).isEqualTo(1.0);
    }

    @Test
    void score_nullResult_returnsNull() {
        ReviewResult scored = scorer.score(null);
        assertThat(scored).isNull();
    }
}