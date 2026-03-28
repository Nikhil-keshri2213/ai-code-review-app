package com.aicodereview.webhook.controller;

import com.aicodereview.webhook.exception.InvalidSignatureException;
import com.aicodereview.webhook.security.GitHubSignatureValidator;
import com.aicodereview.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final GitHubSignatureValidator signatureValidator;
    private final WebhookService webhookService;


    @GetMapping("/health")
    public String health() {
        return "Webhook Service Running";
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestBody byte[] payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType) {

        log.info("Received GitHub webhook — event type: {}, payload size: {} bytes",
                eventType, payload.length);

        // Step 1 — validate HMAC signature
        if (!signatureValidator.isValid(payload, signature)) {
            throw new InvalidSignatureException("Invalid or missing webhook signature");
        }

        // Step 2 — only handle pull_request events
        if (!"pull_request".equals(eventType)) {
            log.info("Ignoring non-PR event type: {}", eventType);
            return ResponseEntity.ok("Event received but not processed: " + eventType);
        }

        // Step 3 — process the PR event
        webhookService.handlePullRequestEvent(payload);

        return ResponseEntity.ok("Webhook processed successfully");
    }
}