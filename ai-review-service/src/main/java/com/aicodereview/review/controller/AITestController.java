package com.aicodereview.review.controller;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.review.client.OpenAIClient;
import com.aicodereview.review.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class AITestController {

    private final OpenAIClient openAIClient;
    private final PromptTemplateService promptTemplateService;

    @GetMapping("/ai")
    public ResponseEntity<String> testAI() {
        log.info("Testing OpenAI connection...");
        String response = openAIClient.chat(
                "You are a helpful assistant.",
                "Say hello in exactly 3 words"
        );
        log.info("OpenAI response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prompt")
    public ResponseEntity<String> testPrompt() {
        log.info("Testing prompt engineering with buggy Java code...");

        // Intentionally buggy Java code for testing
        String buggyCode = """
                public class UserService {
                    private static final String DB_PASSWORD = "admin123";
                    private Connection connection;
                
                    public User getUserById(String id) {
                        String query = "SELECT * FROM users WHERE id = " + id;
                        ResultSet rs = connection.createStatement().executeQuery(query);
                        return mapToUser(rs);
                    }
                
                    public void deleteUser(String id) {
                        connection.createStatement()
                            .execute("DELETE FROM users WHERE id = " + id);
                    }
                
                    private User mapToUser(ResultSet rs) {
                        User user = new User();
                        user.setName(rs.getString("name"));
                        return user;
                    }
                }
                """;

        ReviewRequest request = ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .correlationId(UUID.randomUUID().toString())
                .repoFullName("test/repo")
                .prNumber(1)
                .fileName("UserService.java")
                .fileContent(buggyCode)
                .diffContent("")
                .language("java")
                .chunkIndex(0)
                .totalChunks(1)
                .isChunked(false)
                .build();

        String systemPrompt = promptTemplateService.buildSystemPrompt(request.getLanguage());
        String userPrompt = promptTemplateService.buildUserPrompt(request);

        log.debug("System prompt length: {} chars", systemPrompt.length());
        log.debug("User prompt length: {} chars", userPrompt.length());

        String response = openAIClient.chat(systemPrompt, userPrompt);

        log.info("LLM response:\n{}", response);
        return ResponseEntity.ok(response);
    }
}