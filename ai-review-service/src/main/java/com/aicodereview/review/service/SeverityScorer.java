package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SeverityScorer {

    private static final List<String> HIGH_KEYWORDS = List.of(
            "sql injection", "xss", "cross-site scripting",
            "hardcoded", "hard-coded", "hardcode",
            "password", "secret", "api key", "api_key",
            "null pointer", "nullpointer", "buffer overflow",
            "command injection", "path traversal", "remote code",
            "authentication bypass", "privilege escalation",
            "exposed credential", "sensitive data"
    );

    public ReviewResult score(ReviewResult result) {
        if (result == null) return result;

        Severity originalSeverity = result.getSeverity();
        Severity newSeverity = originalSeverity;
        ReviewCategory newCategory = result.getCategory();
        boolean overridden = false;

        // Rule 1 — category-based rules
        if (result.getCategory() == ReviewCategory.SECURITY) {
            newSeverity = Severity.HIGH;
        } else if (result.getCategory() == ReviewCategory.CODE_STYLE) {
            // Don't downgrade if already set higher by LLM
            if (originalSeverity == Severity.LOW) {
                newSeverity = Severity.LOW;
            }
        }

        // Rule 2 — keyword scan on comment text
        String commentLower = result.getComment() != null
                ? result.getComment().toLowerCase() : "";
        String suggestionLower = result.getSuggestion() != null
                ? result.getSuggestion().toLowerCase() : "";
        String combined = commentLower + " " + suggestionLower;

        for (String keyword : HIGH_KEYWORDS) {
            if (combined.contains(keyword)) {
                newSeverity = Severity.HIGH;
                if (result.getCategory() != ReviewCategory.SECURITY) {
                    newCategory = ReviewCategory.SECURITY;
                }
                break;
            }
        }

        // Check if override happened
        overridden = newSeverity != originalSeverity
                || newCategory != result.getCategory();

        if (overridden) {
            log.info("Severity overridden — file: {}, {} → {}, category: {} → {}",
                    result.getFileName(),
                    originalSeverity, newSeverity,
                    result.getCategory(), newCategory);
        }

        // Set confidence score
        double confidence = overridden ? 0.6 : 1.0;

        return result.toBuilder()
                .severity(newSeverity)
                .category(newCategory)
                .confidenceScore(confidence)
                .build();
    }
}