package com.aicodereview.webhook.controller;

import com.aicodereview.webhook.exception.InvalidSignatureException;
import com.aicodereview.webhook.security.GitHubSignatureValidator;
import com.aicodereview.webhook.service.WebhookService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final GitHubSignatureValidator signatureValidator;
    private final WebhookService webhookService;
    private final MeterRegistry meterRegistry;

    public WebhookController(GitHubSignatureValidator signatureValidator,
                             WebhookService webhookService,
                             MeterRegistry meterRegistry) {
        this.signatureValidator = signatureValidator;
        this.webhookService = webhookService;
        this.meterRegistry = meterRegistry;
    }

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

        // Track every incoming webhook event by type
        Counter.builder("webhook.events.received.total")
                .tag("event", eventType)
                .register(meterRegistry)
                .increment();

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