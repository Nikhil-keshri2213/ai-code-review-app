package com.aicodereview.review.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Slf4j
@Service
public class EmbeddingService {

    private static final int EMBEDDING_DIM = 1536;

    public float[] embedCode(String code) {
        try {
            // Generate deterministic pseudo-embedding from content hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));

            float[] embedding = new float[EMBEDDING_DIM];
            for (int i = 0; i < EMBEDDING_DIM; i++) {
                // Spread hash bytes across embedding dimensions
                embedding[i] = (float)(hash[i % hash.length] & 0xFF) / 255.0f - 0.5f;
            }

            // Normalize
            float norm = 0f;
            for (float v : embedding) norm += v * v;
            norm = (float) Math.sqrt(norm);
            if (norm > 0) for (int i = 0; i < embedding.length; i++) embedding[i] /= norm;

            log.info("***Generated hash embedding of size: {}", embedding.length);
            return embedding;

        } catch (Exception e) {
            log.error("###Failed to generate embedding: {}", e.getMessage());
            return new float[0];
        }
    }
}