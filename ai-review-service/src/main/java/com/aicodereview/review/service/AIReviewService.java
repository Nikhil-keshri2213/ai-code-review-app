package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.common.dto.ReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewService {

    private final LLMService llmService;

    public List<ReviewResult> process(ReviewRequest request) {
        log.info("AI reviewing file: {} for PR#{} in {}",
                request.getFileName(),
                request.getPrNumber(),
                request.getRepoFullName());

        List<ReviewResult> results = llmService.reviewCode(request);

        log.info("AI review complete — {} issues found in file: {}",
                results.size(), request.getFileName());

        return results;
    }
}