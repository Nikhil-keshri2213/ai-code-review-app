package com.aicodereview.storage.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.storage.dto.ReviewResponseDTO;
import com.aicodereview.storage.dto.ReviewSummaryDTO;
import com.aicodereview.storage.entity.Review;
import com.aicodereview.storage.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.aicodereview.common.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

// import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewStorageService {

    private final ReviewRepository reviewRepository;

    public Review saveReview(ReviewResult result) {
        try {
            Review review = Review.builder()
                    .repository(result.getRepoFullName())
                    .prNumber(result.getPrNumber())
                    .fileName(result.getFileName())
                    .comment(result.getComment())
                    .severity(result.getSeverity())
                    .category(result.getCategory())
                    .lineNumber(result.getLineNumber())
                    .suggestion(result.getSuggestion())
                    .llmProvider(result.getLlmProvider())
                    .confidenceScore(result.getConfidenceScore())
                    .status("OPEN")
                    .build();

            Review saved = reviewRepository.save(review);

            log.info("Saved review — id: {}, repo: {}, PR#{}, file: {}, severity: {}",
                    saved.getId(),
                    saved.getRepository(),
                    saved.getPrNumber(),
                    saved.getFileName(),
                    saved.getSeverity());

            return saved;

        } catch (Exception e) {
            log.error("Failed to save review for file: {} — {}",
                    result.getFileName(), e.getMessage());
            throw e;
        }
    }

    public List<ReviewResponseDTO> getReviewsByPR(String repo, Integer prNumber) {
        return reviewRepository
                .findByRepositoryAndPrNumber(repo, prNumber)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ReviewSummaryDTO getSummaryByPR(String repo, Integer prNumber) {
        long total = reviewRepository.countByRepositoryAndPrNumber(repo, prNumber);
        long high = reviewRepository.countByRepositoryAndPrNumberAndSeverity(repo, prNumber, Severity.HIGH);
        long medium = reviewRepository.countByRepositoryAndPrNumberAndSeverity(repo, prNumber, Severity.MEDIUM);
        long low = reviewRepository.countByRepositoryAndPrNumberAndSeverity(repo, prNumber, Severity.LOW);

        String risk = high > 0 ? "HIGH" : medium > 0 ? "MEDIUM" : "LOW";

        return ReviewSummaryDTO.builder()
                .repository(repo)
                .prNumber(prNumber)
                .totalIssues(total)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .overallRisk(risk)
                .build();
    }

    public Page<ReviewResponseDTO> getHistory(String repo, Pageable pageable) {
        return reviewRepository
                .findByRepositoryOrderByCreatedAtDesc(repo, pageable)
                .map(this::toDTO);
    }

    private ReviewResponseDTO toDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .repository(review.getRepository())
                .prNumber(review.getPrNumber())
                .fileName(review.getFileName())
                .comment(review.getComment())
                .severity(review.getSeverity())
                .category(review.getCategory())
                .lineNumber(review.getLineNumber())
                .suggestion(review.getSuggestion())
                .llmProvider(review.getLlmProvider())
                .confidenceScore(review.getConfidenceScore())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}