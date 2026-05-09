package com.aicodereview.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagContextService {

    private final EmbeddingService embeddingService;
    private final WeaviateStorageService weaviateStorageService;

    public String getContext(String fileContent, String repoFullName, String fileName) {
        try {
            // Embed the current file being reviewed
            float[] vector = embeddingService.embedCode(fileContent);
            if (vector.length == 0) {
                log.warn("Empty embedding for file: {}", fileName);
                return "";
            }

            // Query Weaviate for top-3 similar snippets from same repo
            List<Map<String, Object>> similar =
                    weaviateStorageService.findSimilarSnippets(vector, repoFullName, 3);

            if (similar.isEmpty()) {
                log.info("No similar snippets found for: {}", fileName);
                return "";
            }

            log.info("✅ Found {} similar snippets for RAG context — file: {}",
                    similar.size(), fileName);

            // Format context string for LLM prompt
            StringBuilder context = new StringBuilder();
            context.append("## Similar Code Patterns from this Repository\n\n");
            context.append("The following files from the same repository may be relevant:\n\n");

            for (int i = 0; i < similar.size(); i++) {
                Map<String, Object> snippet = similar.get(i);
                String snippetFile = (String) snippet.getOrDefault("fileName", "unknown");
                String snippetLang = (String) snippet.getOrDefault("language", "unknown");
                String snippetContent = (String) snippet.getOrDefault("content", "");

                // Truncate content to 500 chars to stay within token limits
                if (snippetContent.length() > 500) {
                    snippetContent = snippetContent.substring(0, 500) + "...";
                }

                context.append(String.format("### Similar File %d: %s (%s)\n", 
                        i + 1, snippetFile, snippetLang));
                context.append("```\n");
                context.append(snippetContent);
                context.append("\n```\n\n");
            }

            return context.toString();

        } catch (Exception e) {
            log.error("RAG context retrieval failed for {}: {}", fileName, e.getMessage());
            return "";
        }
    }
}