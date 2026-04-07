package com.aicodereview.analysis.service;

import com.aicodereview.common.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeAnalysisService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.review-tasks}")
    private String reviewTasksTopic;

    public void process(ReviewRequest request) {
        log.info("Processing ReviewRequest — correlationId: {}, file: {}, " +
                        "language: {}, chunk: {}/{}, isChunked: {}",
                request.getCorrelationId(),
                request.getFileName(),
                request.getLanguage(),
                request.getChunkIndex() + 1,
                request.getTotalChunks(),
                request.isChunked()
        );

        log.debug("File content length: {} chars, Diff content length: {} chars",
                request.getFileContent() != null ? request.getFileContent().length() : 0,
                request.getDiffContent() != null ? request.getDiffContent().length() : 0
        );

        // TODO: Day 13 — AI Review Service consumes from review-tasks topic
        publishToReviewTasks(request);
    }

    private void publishToReviewTasks(ReviewRequest request) {
        String messageKey = request.isChunked()
                ? request.getFileName() + "-chunk-" + request.getChunkIndex()
                : request.getFileName();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(reviewTasksTopic, messageKey, request);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Forwarded ReviewRequest to review-tasks — " +
                                "file: {}, chunk: {}/{}, partition: {}, offset: {}",
                        request.getFileName(),
                        request.getChunkIndex() + 1,
                        request.getTotalChunks(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("Failed to forward ReviewRequest — file: {}, error: {}",
                        request.getFileName(), ex.getMessage());
            }
        });
    }
}