package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewComment;
import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import com.aicodereview.review.client.OpenAIClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    private final OpenAIClient openAIClient;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;

    public List<ReviewResult> reviewCode(ReviewRequest request) {
        log.info("Calling LLM for file: {} lang: {} chunk: {}/{}",
                request.getFileName(),
                request.getLanguage(),
                request.getChunkIndex() + 1,
                request.getTotalChunks());

        try {
            // Build prompts
            String systemPrompt = promptTemplateService
                    .buildSystemPrompt(request.getLanguage());
            String userPrompt = promptTemplateService
                    .buildUserPrompt(request);

            // Call LLM
            String rawResponse = openAIClient.chat(systemPrompt, userPrompt);
            log.debug("Raw LLM response: {}", rawResponse);

            // Parse response
            List<ReviewComment> comments = parseResponse(rawResponse);
            log.info("Parsed {} issues from LLM for file: {}",
                    comments.size(), request.getFileName());

            // Map to ReviewResult
            List<ReviewResult> results = mapToResults(comments, request);

            // Handle empty response
            if (results.isEmpty()) {
                log.info("No issues found for file: {} — adding placeholder",
                        request.getFileName());
                results.add(buildNoIssuesResult(request));
            }

            return results;

        } catch (Exception e) {
            log.error("LLM review failed for file: {} — {}",
                    request.getFileName(), e.getMessage());
            return List.of(buildErrorResult(request, e.getMessage()));
        }
    }

    // ── Parse LLM response — handle all edge cases ──
    private List<ReviewComment> parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("LLM returned empty response");
            return List.of();
        }

        String cleaned = rawResponse.trim();

        // Strip markdown code fences if present
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            int lastFence = cleaned.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                cleaned = cleaned.substring(firstNewline + 1, lastFence).trim();
            }
        }

        // Handle empty array
        if (cleaned.equals("[]") || cleaned.isEmpty()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(cleaned,
                    new TypeReference<List<ReviewComment>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response: {} — raw: {}",
                    e.getMessage(), cleaned.substring(0, Math.min(200, cleaned.length())));
            return List.of();
        }
    }

    // ── Map ReviewComment → ReviewResult ──
    private List<ReviewResult> mapToResults(List<ReviewComment> comments,
                                             ReviewRequest request) {
        List<ReviewResult> results = new ArrayList<>();
        for (ReviewComment comment : comments) {
            results.add(ReviewResult.builder()
                    .resultId(UUID.randomUUID())
                    .requestId(request.getRequestId())
                    .repoFullName(request.getRepoFullName())
                    .prNumber(request.getPrNumber())
                    .fileName(comment.getFileName() != null
                            ? comment.getFileName()
                            : request.getFileName())
                    .comment(comment.getComment())
                    .severity(parseSeverity(comment.getSeverity()))
                    .category(parseCategory(comment.getCategory()))
                    .lineNumber(comment.getLineNumber())
                    .suggestion(comment.getSuggestion())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        return results;
    }

    private Severity parseSeverity(String s) {
        if (s == null) return Severity.LOW;
        try {
            return Severity.valueOf(s.toUpperCase().trim());
        } catch (Exception e) {
            return Severity.LOW;
        }
    }

    private ReviewCategory parseCategory(String c) {
        if (c == null) return ReviewCategory.OTHER;
        try {
            return ReviewCategory.valueOf(c.toUpperCase().trim());
        } catch (Exception e) {
            return ReviewCategory.OTHER;
        }
    }

    private ReviewResult buildNoIssuesResult(ReviewRequest request) {
        return ReviewResult.builder()
                .resultId(UUID.randomUUID())
                .requestId(request.getRequestId())
                .repoFullName(request.getRepoFullName())
                .prNumber(request.getPrNumber())
                .fileName(request.getFileName())
                .comment("No issues found in this file")
                .severity(Severity.LOW)
                .category(ReviewCategory.OTHER)
                .suggestion("Code looks good!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ReviewResult buildErrorResult(ReviewRequest request, String error) {
        return ReviewResult.builder()
                .resultId(UUID.randomUUID())
                .requestId(request.getRequestId())
                .repoFullName(request.getRepoFullName())
                .prNumber(request.getPrNumber())
                .fileName(request.getFileName())
                .comment("Review failed: " + error)
                .severity(Severity.LOW)
                .category(ReviewCategory.OTHER)
                .createdAt(LocalDateTime.now())
                .build();
    }
}