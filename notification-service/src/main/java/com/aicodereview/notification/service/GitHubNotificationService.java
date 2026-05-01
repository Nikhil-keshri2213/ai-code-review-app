package com.aicodereview.notification.service;

import com.aicodereview.common.dto.ReviewResult;
import com.aicodereview.common.enums.Severity;
import com.aicodereview.notification.client.GitHubCommentClient;
import com.aicodereview.notification.dto.GitHubCommentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubNotificationService {

    private final GitHubCommentClient gitHubCommentClient;

    public void postReviewComment(ReviewResult result) {
    try {
        String[] parts = result.getRepoFullName().split("/");
        String owner = parts[0];
        String repo  = parts[1];

        String body = formatComment(result);

        // Use Issue Comments API — simpler, no commitSha or line needed
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

    } catch (Exception e) {
        log.error("Failed to post GitHub comment for PR#{}: {}",
                result.getPrNumber(), e.getMessage());
    }
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