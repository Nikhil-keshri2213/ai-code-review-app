package com.aicodereview.notification.service;

import com.aicodereview.common.dto.ReviewResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public void sendReviewNotification(ReviewResult result) {
        try {
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
                emoji,
                result.getRepoFullName(),
                prLink, result.getRepoFullName(), result.getPrNumber(),
                result.getFileName(),
                emoji, result.getSeverity(),
                result.getComment(),
                result.getSuggestion() != null ? result.getSuggestion() : "N/A",
                result.getConfidenceScore() != null ? result.getConfidenceScore() * 100 : 0.0
            );

            Map<String, String> payload = Map.of("text", message);

            webClient.post()
                    .uri("")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("✅ Slack notification sent for PR#{} file: {}",
                    result.getPrNumber(), result.getFileName());

        } catch (Exception e) {
            log.error("Failed to send Slack notification for PR#{}: {}",
                    result.getPrNumber(), e.getMessage());
        }
    }
}