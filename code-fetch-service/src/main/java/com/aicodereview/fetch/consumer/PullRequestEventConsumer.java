package com.aicodereview.fetch.consumer;

import com.aicodereview.fetch.service.CodeFetchService;
import com.aicodereview.common.dto.PullRequestEvent;
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
public class PullRequestEventConsumer {

    private final CodeFetchService codeFetchService;

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
        log.info("PR event received from Kafka");
        log.info("Correlation ID : {}", event.getCorrelationId());
        log.info("Repo           : {}", event.getRepoFullName());
        log.info("PR Number      : #{}", event.getPrNumber());
        log.info("Action         : {}", event.getAction());
        log.info("Partition      : {}, Offset: {}", partition, offset);
        log.info("============================================");

        codeFetchService.fetchAndPublish(event);
    }
}