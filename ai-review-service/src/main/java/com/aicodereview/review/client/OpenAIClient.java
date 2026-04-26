package com.aicodereview.review.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OpenAIClient {

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final ObjectMapper mapper;

    @Value("${ai.provider}")
    private String activeProvider;

    @Value("${ai.providers.groq.base-url}")
    private String groqBaseUrl;
    @Value("${ai.providers.groq.api-key}")
    private String groqApiKey;
    @Value("${ai.providers.groq.model}")
    private String groqModel;

    @Value("${ai.providers.openai.base-url}")
    private String openaiBaseUrl;
    @Value("${ai.providers.openai.api-key}")
    private String openaiApiKey;
    @Value("${ai.providers.openai.model}")
    private String openaiModel;

    @Value("${ai.providers.ollama.base-url}")
    private String ollamaBaseUrl;
    @Value("${ai.providers.ollama.api-key}")
    private String ollamaApiKey;
    @Value("${ai.providers.ollama.model}")
    private String ollamaModel;

    public OpenAIClient() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    public String getProviderName() {
        return activeProvider;
    }

    private String getBaseUrl() {
        return switch (activeProvider.toLowerCase()) {
            case "openai" -> openaiBaseUrl;
            case "ollama" -> ollamaBaseUrl;
            default -> groqBaseUrl;
        };
    }

    private String getApiKey() {
        return switch (activeProvider.toLowerCase()) {
            case "openai" -> openaiApiKey;
            case "ollama" -> ollamaApiKey;
            default -> groqApiKey;
        };
    }

    private String getModel() {
        return switch (activeProvider.toLowerCase()) {
            case "openai" -> openaiModel;
            case "ollama" -> ollamaModel;
            default -> groqModel;
        };
    }

    public String chat(String systemPrompt, String userPrompt) {
        String url = getBaseUrl() + "/chat/completions";
        String model = getModel();

        log.info("Calling AI provider: {} model: {}", activeProvider, model);

        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.1);
            body.put("max_tokens", 2000);

            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userPrompt);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(mapper.writeValueAsString(body), JSON_TYPE))
                    .build();

            try (Response response = http.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    log.error("AI API error — provider: {}, status: {}, body: {}",
                            activeProvider, response.code(), responseBody);
                    throw new RuntimeException("AI API error " + response.code()
                            + ": " + responseBody);
                }
                log.info("AI response status: {}", response.code());
                JsonNode json = mapper.readTree(responseBody);
                return json.path("choices").get(0)
                        .path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("AI call failed — provider: {}, error: {}",
                    activeProvider, e.getMessage());
            throw new RuntimeException("AI call failed", e);
        }
    }
}