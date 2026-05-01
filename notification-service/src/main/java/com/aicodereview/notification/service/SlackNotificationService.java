package com.aicodereview.notification.service;

import com.aicodereview.common.dto.ReviewResult;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class SlackNotificationService {

    private final WebClient webClient;

    public SlackNotificationService(
            @Value("${slack.webhook.url}") String webhookUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(webhookUrl)
                .build();
    }

    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendReviewNotification(ReviewResult result) {
        String emoji = switch (result.getSeverity()) {
            case HIGH   -> "🔴";
            case MEDIUM -> "🟡";
            case LOW    -> "🟢";
        };

        String prLink = String.format(
            "https://github.com/%s/pull/%d",
            result.getRepoFullName(),
            result.getPrNumber()
        );

        String message = String.format("""
                %s *AI Code Review — %s*

                *PR:* <%s|%s #%d>
                *File:* `%s`
                *Severity:* %s %s
                *Finding:* %s
                *Suggestion:* %s
                *Confidence:* %.0f%%
                """,
                emoji, result.getRepoFullName(),
                prLink, result.getRepoFullName(), result.getPrNumber(),
                result.getFileName(),
                emoji, result.getSeverity(),
                result.getComment(),
                result.getSuggestion() != null ? result.getSuggestion() : "N/A",
                result.getConfidenceScore() != null ? result.getConfidenceScore() * 100 : 0.0
        );

        webClient.post()
                .uri("")
                .bodyValue(Map.of("text", message))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("✅ Slack notification sent for PR#{} file: {}",
                result.getPrNumber(), result.getFileName());
    }

    @Recover
    public void recoverSlack(Exception e, ReviewResult result) {
        log.error("❌ Slack notification permanently failed for PR#{} after 3 attempts: {}",
                result.getPrNumber(), e.getMessage());
    }
}