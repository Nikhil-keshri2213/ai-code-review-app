package com.aicodereview.fetch.consumer;

import com.aicodereview.common.dto.PullRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PullRequestEventConsumer {

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
        log.info("Received PR event from Kafka");
        log.info("Correlation ID : {}", event.getCorrelationId());
        log.info("Repo           : {}", event.getRepoFullName());
        log.info("PR Number      : {}", event.getPrNumber());
        log.info("PR Title       : {}", event.getPrTitle());
        log.info("Action         : {}", event.getAction());
        log.info("Head SHA       : {}", event.getHeadSha());
        log.info("Branch         : {} → {}", event.getHeadRef(), event.getBaseRef());
        log.info("Sender         : {}", event.getSenderLogin());
        log.info("Partition      : {}, Offset: {}", partition, offset);
        log.info("============================================");

        // Day 8 — call GitHub API here to fetch PR files
        log.info("TODO: fetch PR files from GitHub API for PR#{}",
                event.getPrNumber());
    }
}

// Your `code-fetch-service` package structure should now look like this:
// ```
// code-fetch-service/src/main/java/com/ai/codereview/codefetch/
// ├── CodeFetchServiceApplication.java
// ├── config/
// │   ├── KafkaConsumerConfig.java
// │   └── KafkaTopicConfig.java
// └── consumer/
//     └── PullRequestEventConsumer.java