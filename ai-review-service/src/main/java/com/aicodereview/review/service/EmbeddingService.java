package com.aicodereview.review.service;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    private final OpenAiService openAiService;

    public EmbeddingService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public float[] embedCode(String code) {
        try {
            // Truncate to 8000 chars to stay within token limit
            String truncated = code.length() > 8000
                    ? code.substring(0, 8000)
                    : code;

            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model("text-embedding-3-small")
                    .input(List.of(truncated))
                    .build();

            var result = openAiService.createEmbeddings(request);
            List<Double> embedding = result.getData().get(0).getEmbedding();

            float[] floatArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                floatArray[i] = embedding.get(i).floatValue();
            }

            log.info("✅ Generated embedding of size: {}", floatArray.length);
            return floatArray;

        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage());
            return new float[0];
        }
    }
}