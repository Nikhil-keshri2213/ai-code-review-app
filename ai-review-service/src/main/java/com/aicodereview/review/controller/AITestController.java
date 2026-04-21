package com.aicodereview.review.controller;

import com.aicodereview.review.client.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class AITestController {

    private final OpenAIClient openAIClient;

    @GetMapping("/ai")
    public ResponseEntity<String> testAI() {
        log.info("Testing OpenAI...");
        String response = openAIClient.chat(
                "You are a helpful assistant.",
                "Say hello in exactly 3 words"
        );
        log.info("OpenAI responded: {}", response);
        return ResponseEntity.ok(response);
    }
}