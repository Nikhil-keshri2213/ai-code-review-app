package com.aicodereview.review.consumer;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.review.service.AIReviewService;
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

    private final AIReviewService aiReviewService;

    @KafkaListener(
            topics = "${kafka.topics.review-tasks}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload ReviewRequest request,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("ReviewRequest received — file: {}, lang: {}, chunk: {}/{}",
                request.getFileName(), request.getLanguage(),
                request.getChunkIndex() + 1, request.getTotalChunks());

        aiReviewService.process(request);
    }
}