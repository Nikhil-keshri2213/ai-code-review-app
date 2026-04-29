package com.aicodereview.storage.consumer;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.storage.service.ReviewStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewResultConsumer {

    private final ReviewStorageService reviewStorageService;

    @KafkaListener(
            topics = "${kafka.topics.review-results}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload ReviewResult result,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("ReviewResult received — repo: {}, PR#{}, file: {}, severity: {}, provider: {}",
                result.getRepoFullName(),
                result.getPrNumber(),
                result.getFileName(),
                result.getSeverity(),
                result.getLlmProvider());

        reviewStorageService.saveReview(result);
    }
}