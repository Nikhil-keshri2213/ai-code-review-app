package com.aicodereview.storage.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import com.aicodereview.storage.entity.Review;
import com.aicodereview.storage.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "spring.profiles.active=test",
                "spring.docker.host=npipe:////./pipe/docker_engine"
        }
)
@Testcontainers(disabledWithoutDocker = true)
class ReviewStorageServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("codereview")
                    .withUsername("codereview")
                    .withPassword("secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Disable Kafka auto-connect in tests
        registry.add("spring.kafka.bootstrap-servers",
                () -> "localhost:9999");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    @Autowired
    private ReviewStorageService reviewStorageService;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void saveReview_persistsToDatabase() {
        ReviewResult result = buildReviewResult("HIGH", "SECURITY",
                "SQL injection found", "Use PreparedStatement");

        Review saved = reviewStorageService.saveReview(result);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRepository()).isEqualTo("test/repo");
        assertThat(saved.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(saved.getCategory()).isEqualTo(ReviewCategory.SECURITY);
        assertThat(saved.getLlmProvider()).isEqualTo("groq");
        assertThat(saved.getConfidenceScore()).isEqualTo(1.0);
    }

    @Test
    void saveReview_canBeQueryedBack() {
        ReviewResult result = buildReviewResult("MEDIUM", "BUG",
                "NPE risk", "Add null check");

        reviewStorageService.saveReview(result);

        List<Review> found = reviewRepository
                .findByRepositoryAndPrNumber("test/repo", 42);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getComment()).isEqualTo("NPE risk");
        assertThat(found.get(0).getSuggestion()).isEqualTo("Add null check");
    }

    @Test
    void saveMultipleReviews_countCorrect() {
        reviewStorageService.saveReview(
                buildReviewResult("HIGH", "SECURITY", "Issue 1", "Fix 1"));
        reviewStorageService.saveReview(
                buildReviewResult("MEDIUM", "BUG", "Issue 2", "Fix 2"));
        reviewStorageService.saveReview(
                buildReviewResult("LOW", "CODE_STYLE", "Issue 3", "Fix 3"));

        long count = reviewRepository
                .countByRepositoryAndPrNumber("test/repo", 42);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void saveReview_statusDefaultsToOpen() {
        Review saved = reviewStorageService.saveReview(
                buildReviewResult("LOW", "OTHER", "Minor issue", "Fix it"));

        assertThat(saved.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void getReviewsByPR_returnsCorrectDTOs() {
        reviewStorageService.saveReview(
                buildReviewResult("HIGH", "SECURITY", "XSS found", "Sanitize"));

        var reviews = reviewStorageService.getReviewsByPR("test/repo", 42);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(reviews.get(0).getComment()).isEqualTo("XSS found");
    }

    @Test
    void getSummaryByPR_correctCounts() {
        reviewStorageService.saveReview(
                buildReviewResult("HIGH", "SECURITY", "Issue 1", "Fix 1"));
        reviewStorageService.saveReview(
                buildReviewResult("HIGH", "SECURITY", "Issue 2", "Fix 2"));
        reviewStorageService.saveReview(
                buildReviewResult("LOW", "CODE_STYLE", "Issue 3", "Fix 3"));

        var summary = reviewStorageService.getSummaryByPR("test/repo", 42);

        assertThat(summary.getTotalIssues()).isEqualTo(3);
        assertThat(summary.getHighCount()).isEqualTo(2);
        assertThat(summary.getLowCount()).isEqualTo(1);
        assertThat(summary.getOverallRisk()).isEqualTo("HIGH");
    }

    private ReviewResult buildReviewResult(String severity,
                                            String category,
                                            String comment,
                                            String suggestion) {
        return ReviewResult.builder()
                .resultId(UUID.randomUUID())
                .requestId(UUID.randomUUID())
                .repoFullName("test/repo")
                .prNumber(42)
                .fileName("Test.java")
                .comment(comment)
                .severity(Severity.valueOf(severity))
                .category(ReviewCategory.valueOf(category))
                .suggestion(suggestion)
                .llmProvider("groq")
                .confidenceScore(1.0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}