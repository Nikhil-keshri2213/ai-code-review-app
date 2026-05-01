package com.aicodereview.notification.consumer;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.notification.service.GitHubNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final GitHubNotificationService gitHubNotificationService;

    @KafkaListener(
            topics = "review-results",
            groupId = "notification-service-group"
    )
    public void consume(ReviewResult result) {
        log.info("Consumed ReviewResult for PR#{} file: {}",
                result.getPrNumber(), result.getFileName());
        gitHubNotificationService.postReviewComment(result);
    }
}