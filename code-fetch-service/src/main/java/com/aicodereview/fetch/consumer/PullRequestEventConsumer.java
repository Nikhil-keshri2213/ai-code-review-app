package com.aicodereview.fetch.consumer;

import com.aicodereview.fetch.client.GitHubApiClient;
import com.aicodereview.fetch.dto.GitHubPRFile;
import com.aicodereview.common.dto.PullRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PullRequestEventConsumer {

    private final GitHubApiClient gitHubApiClient;

    @KafkaListener(
            topics = "${kafka.topics.pr-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload PullRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("============================================");
        log.info("Received PR event — correlationId: {}",
                event.getCorrelationId());
        log.info("Repo: {}, PR#: {}, Action: {}",
                event.getRepoFullName(),
                event.getPrNumber(),
                event.getAction());
        log.info("Partition: {}, Offset: {}", partition, offset);
        log.info("============================================");

        // Fetch changed files from GitHub API
        List<GitHubPRFile> files = gitHubApiClient
                .getPullRequestFiles(
                        event.getRepoFullName(),
                        event.getPrNumber()
                );

        if (files.isEmpty()) {
            log.warn("No files found for PR#{} — skipping",
                    event.getPrNumber());
            return;
        }

        log.info("Found {} changed files in PR#{}",
                files.size(), event.getPrNumber());

        files.forEach(file -> {
            if (file.isModifiedOrAdded()) {
                log.info("  [{}] {} — +{} -{} lines | language: {}",
                        file.getStatus(),
                        file.getFilename(),
                        file.getAdditions(),
                        file.getDeletions(),
                        file.detectLanguage()
                );
            } else {
                log.info("  [SKIPPED - {}] {}", file.getStatus(),
                        file.getFilename());
            }
        });

        // Day 9 — publish to code-analysis-tasks topic
        log.info("TODO: publish {} files to code-analysis-tasks topic",
                files.stream().filter(GitHubPRFile::isModifiedOrAdded).count());
    }
}


// Your final structure for `code-fetch-service`:
// ```
// codefetch/
// ├── CodeFetchServiceApplication.java
// ├── client/
// │   └── GitHubApiClient.java
// ├── config/
// │   ├── KafkaConsumerConfig.java
// │   ├── KafkaTopicConfig.java
// │   └── WebClientConfig.java
// ├── consumer/
// │   └── PullRequestEventConsumer.java
// └── dto/
//     └── GitHubPRFile.java