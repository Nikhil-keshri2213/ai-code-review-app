package com.aicodereview.review.consumer;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.review.service.AIReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRequestConsumer {

    private final AIReviewService aiReviewService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.review-results}")
    private String reviewResultsTopic;

    @KafkaListener(
            topics = "${kafka.topics.review-tasks}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload ReviewRequest request,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("============================================");
        log.info("ReviewRequest received from Kafka");
        log.info("Correlation ID : {}", request.getCorrelationId());
        log.info("Repo           : {}", request.getRepoFullName());
        log.info("PR Number      : #{}", request.getPrNumber());
        log.info("File           : {}", request.getFileName());
        log.info("Language       : {}", request.getLanguage());
        log.info("Chunk          : {}/{}",
                request.getChunkIndex() + 1, request.getTotalChunks());
        log.info("Partition      : {}, Offset: {}", partition, offset);
        log.info("============================================");

        // Call AI review
        List<ReviewResult> results = aiReviewService.process(request);

        // Publish each result to review-results topic
        publishResults(results, request.getCorrelationId());
    }

    private void publishResults(List<ReviewResult> results, String correlationId) {
        log.info("Publishing {} ReviewResults to review-results topic", results.size());

        for (ReviewResult result : results) {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(reviewResultsTopic, correlationId, result);

            future.whenComplete((sendResult, ex) -> {
                if (ex == null) {
                    log.info("Published ReviewResult — severity: {}, file: {}, " +
                                    "partition: {}, offset: {}",
                            result.getSeverity(),
                            result.getFileName(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset()
                    );
                } else {
                    log.error("Failed to publish ReviewResult: {}", ex.getMessage());
                }
            });
        }
    }
}