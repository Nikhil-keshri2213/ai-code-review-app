package com.aicodereview.analysis.consumer;

import com.aicodereview.analysis.service.CodeAnalysisService;
import com.aicodereview.common.dto.ReviewRequest;
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
public class ReviewRequestConsumer {

    private final CodeAnalysisService codeAnalysisService;

    @KafkaListener(
            topics = "${kafka.topics.code-analysis-tasks}",
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

        codeAnalysisService.process(request);
    }
}