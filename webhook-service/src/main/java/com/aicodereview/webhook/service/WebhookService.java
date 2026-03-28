package com.aicodereview.webhook.service;

import com.aicodereview.common.dto.PullRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Set<String> PROCESSABLE_ACTIONS = Set.of("opened", "synchronize", "reopened");

    private final ObjectMapper objectMapper;

    public void handlePullRequestEvent(byte[] payload) {
        try {
            PullRequestEvent event = objectMapper.readValue(payload, PullRequestEvent.class);

            log.info("PR Event received — action: {}, repo: {}, PR#: {}, by: {}",
                    event.getAction(),
                    event.getRepoFullName(),
                    event.getPrNumber(),
                    event.getSenderLogin()
            );

            if (!PROCESSABLE_ACTIONS.contains(event.getAction())) {
                log.info("Ignoring PR action '{}' — only processing: {}",
                        event.getAction(), PROCESSABLE_ACTIONS);
                return;
            }

            log.info("Processing PR#{} — head SHA: {}, branch: {} → {}",
                    event.getPrNumber(),
                    event.getHeadSha(),
                    event.getHeadRef(),
                    event.getBaseRef()
            );

            // Day 6 — publish to Kafka here
            log.info("TODO: publish event to Kafka topic pr-events");

        } catch (Exception e) {
            log.error("Failed to parse PR event payload: {}", e.getMessage());
            throw new RuntimeException("Failed to process webhook payload", e);
        }
    }
}