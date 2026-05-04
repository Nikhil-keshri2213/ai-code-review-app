package com.aicodereview.review.service;

import com.aicodereview.review.util.LanguageDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoIndexerService {

    private final WeaviateStorageService weaviateStorageService;
    private final EmbeddingService embeddingService;
    private final LanguageDetector languageDetector;

    // In-memory cache to avoid re-indexing in same session
    private final ConcurrentHashMap<String, Boolean> indexedRepos = new ConcurrentHashMap<>();

    @Value("${github.token}")
    private String githubToken;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Authorization", "Bearer " + "")
            .defaultHeader("Accept", "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .build();

    public void indexRepository(String repoFullName, Integer prNumber) {
        // Skip if already indexed this session
        if (indexedRepos.containsKey(repoFullName)) {
            log.info("Repo {} already indexed this session — skipping", repoFullName);
            return;
        }

        // Skip if already indexed in Weaviate
        if (weaviateStorageService.isRepoIndexed(repoFullName)) {
            log.info("Repo {} already indexed in Weaviate — skipping", repoFullName);
            indexedRepos.put(repoFullName, true);
            return;
        }

        log.info("🔍 Starting repo indexing for: {}", repoFullName);

        try {
            // Fetch repo file tree
            String[] parts = repoFullName.split("/");
            String owner = parts[0];
            String repo  = parts[1];

            List<Map<String, Object>> tree = fetchRepoTree(owner, repo);
            if (tree == null || tree.isEmpty()) {
                log.warn("No files found for repo: {}", repoFullName);
                return;
            }

            int indexed = 0;
            for (Map<String, Object> file : tree) {
                String type = (String) file.get("type");
                String path = (String) file.get("path");

                // Only index code files
                if (!"blob".equals(type) || !languageDetector.isCodeFile(path)) continue;

                // Skip large files
                Object sizeObj = file.get("size");
                if (sizeObj != null && ((Number) sizeObj).intValue() > 50000) continue;

                try {
                    String content = fetchFileContent(owner, repo, path);
                    if (content == null || content.isBlank()) continue;

                    String language = languageDetector.detect(path);
                    float[] vector = embeddingService.embedCode(content);

                    if (vector.length > 0) {
                        weaviateStorageService.storeCodeSnippet(
                                content, path, repoFullName, language, prNumber, vector);
                        indexed++;
                        log.debug("Indexed: {}", path);
                    }
                } catch (Exception e) {
                    log.warn("Failed to index file {}: {}", path, e.getMessage());
                }
            }

            indexedRepos.put(repoFullName, true);
            log.info("✅ Indexed {} files for repo: {}", indexed, repoFullName);

        } catch (Exception e) {
            log.error("❌ Failed to index repo {}: {}", repoFullName, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchRepoTree(String owner, String repo) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .defaultHeader("Authorization", "Bearer " + githubToken)
                    .defaultHeader("Accept", "application/vnd.github+json")
                    .build();

            Map<String, Object> response = client.get()
                    .uri("/repos/{owner}/{repo}/git/trees/HEAD?recursive=1", owner, repo)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("tree")) {
                return (List<Map<String, Object>>) response.get("tree");
            }
        } catch (Exception e) {
            log.error("Failed to fetch repo tree: {}", e.getMessage());
        }
        return List.of();
    }

    private String fetchFileContent(String owner, String repo, String path) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .defaultHeader("Authorization", "Bearer " + githubToken)
                    .defaultHeader("Accept", "application/vnd.github.raw+json")
                    .build();

            return client.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to fetch {}: {}", path, e.getMessage());
            return null;
        }
    }
}