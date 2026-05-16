package com.aicodereview.webhook.service;

import com.aicodereview.common.dto.PullRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Set<String> PROCESSABLE_ACTIONS =
            Set.of("opened", "synchronize", "reopened");

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.pr-events}")
    private String prEventsTopic;

    public void handlePullRequestEvent(byte[] payload) {
        try {
            PullRequestEvent event = objectMapper
                    .readValue(payload, PullRequestEvent.class);

            log.info(
                    "PR Event received — action: {}, repo: {}, PR#: {}, by: {}",
                    event.getAction(),
                    event.getRepoFullName(),
                    event.getPrNumber(),
                    event.getSenderLogin()
            );

            if (!PROCESSABLE_ACTIONS.contains(event.getAction())) {
                log.info(
                        "Ignoring PR action '{}' — only processing: {}",
                        event.getAction(),
                        PROCESSABLE_ACTIONS
                );
                return;
            }

            // generate correlation id
            event.setCorrelationId(UUID.randomUUID().toString());

            publishToKafka(event);

        } catch (Exception e) {
            log.error(
                    "Failed to process PR event: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Failed to process webhook payload",
                    e
            );
        }
    }

    private void publishToKafka(PullRequestEvent event) {

        // Better partition distribution
        String messageKey =
                event.getRepoFullName() + "-" + event.getPrNumber();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        prEventsTopic,
                        messageKey,
                        event
                );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "Published to Kafka — key: {}, topic: {}, partition: {}, offset: {}",
                        messageKey,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error(
                        "Failed to publish to Kafka: {}",
                        ex.getMessage(),
                        ex
                );
            }
        });
    }
}