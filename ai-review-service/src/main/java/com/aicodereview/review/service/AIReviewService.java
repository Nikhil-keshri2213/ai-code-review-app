package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.review.client.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewService {

    private final OpenAIClient openAIClient;

    public void process(ReviewRequest request) {
        log.info("Processing file: {} with AI", request.getFileName());
        // Day 14 — prompt engineering wired here
        log.info("TODO: Day 14 - build prompt and call LLM");
    }
}