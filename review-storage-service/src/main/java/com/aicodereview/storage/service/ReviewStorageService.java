package com.aicodereview.storage.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.storage.entity.Review;
import com.aicodereview.storage.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

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
                    // .confidenceScore(result.getConfidenceScore())
                    .confidenceScore(result.getConfidenceScore() != null 
                    ? BigDecimal.valueOf(result.getConfidenceScore()) 
                    : null)
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
}