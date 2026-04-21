package com.aicodereview.review.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OpenAIClient {

    private static final MediaType JSON_TYPE =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final ObjectMapper mapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${openai.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    public OpenAIClient() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("AI Client Configuration");
        log.info("Base URL: {}", baseUrl);
        log.info("Model: {}", model);
        log.info("API Key present: {}", apiKey != null && !apiKey.isBlank());
        log.info("API Key prefix: {}",
                apiKey != null && apiKey.length() > 7
                        ? apiKey.substring(0, 7) + "..."
                        : "MISSING");
        log.info("========================================");
    }

    public String chat(String systemPrompt, String userPrompt) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.1);
            body.put("max_tokens", 2000);

            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userPrompt);

            String requestBody = mapper.writeValueAsString(body);
            String fullUrl = baseUrl + "/chat/completions";

            log.info("Calling AI endpoint: {}", fullUrl);
            log.debug("Request body: {}", requestBody);

            Request request = new Request.Builder()
                    .url(fullUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON_TYPE))
                    .build();

            try (Response response = http.newCall(request).execute()) {
                String responseBody = response.body() != null
                        ? response.body().string()
                        : "";

                log.info("AI response status: {}", response.code());

                if (!response.isSuccessful()) {
                    log.error("AI API error — status: {}, body: {}",
                            response.code(), responseBody);
                    throw new RuntimeException("AI error " + response.code()
                            + ": " + responseBody);
                }

                log.debug("AI response body: {}", responseBody);
                JsonNode json = mapper.readTree(responseBody);
                return json.path("choices").get(0)
                        .path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI call failed: " + e.getMessage(), e);
        }
    }
}