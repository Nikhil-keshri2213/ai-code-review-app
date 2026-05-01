package com.aicodereview.notification.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.notification.client.GitHubCommentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubNotificationService {

    private final GitHubCommentClient gitHubCommentClient;

    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void postReviewComment(ReviewResult result) {
        String[] parts = result.getRepoFullName().split("/");
        String owner = parts[0];
        String repo  = parts[1];

        String body = formatComment(result);
        Map<String, String> request = Map.of("body", body);

        gitHubCommentClient.getClient()
                .post()
                .uri("/repos/{owner}/{repo}/issues/{prNumber}/comments",
                        owner, repo, result.getPrNumber())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("✅ Posted GitHub comment for PR#{} file: {}",
                result.getPrNumber(), result.getFileName());
    }

    @Recover
    public void recoverGitHub(Exception e, ReviewResult result) {
        log.error("❌ GitHub comment permanently failed for PR#{} after 3 attempts: {}",
                result.getPrNumber(), e.getMessage());
    }

    private String formatComment(ReviewResult result) {
        String emoji = switch (result.getSeverity()) {
            case HIGH   -> "🔴 **HIGH**";
            case MEDIUM -> "🟡 **MEDIUM**";
            case LOW    -> "🟢 **LOW**";
        };

        return String.format("""
                %s — %s

                %s

                💡 **Suggestion:** %s

                *AI Code Review Bot · Confidence: %.0f%%*
                """,
                emoji,
                result.getCategory(),
                result.getComment(),
                result.getSuggestion() != null ? result.getSuggestion() : "No suggestion",
                result.getConfidenceScore() != null ? result.getConfidenceScore() * 100 : 0.0
        );
    }
}