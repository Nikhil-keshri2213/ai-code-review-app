package com.aicodereview.fetch.client;

import com.aicodereview.fetch.dto.GitHubPRFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClient {

    private final WebClient githubWebClient;

    public List<GitHubPRFile> getPullRequestFiles(String repoFullName,
            Integer prNumber) {
        log.info("Fetching PR files — repo: {}, PR#: {}", repoFullName, prNumber);

        try {
            List<GitHubPRFile> files = githubWebClient.get()
                    // .uri("/repos/{repo}/pulls/{prNumber}/files",
                    //         repoFullName, prNumber)
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}/files",
                        repoFullName.split("/")[0],
                        repoFullName.split("/")[1],
                        prNumber) //updated
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GitHubPRFile>>() {
                    })
                    .block();

            if (files == null)
                return Collections.emptyList();

            log.info("Fetched {} files for PR#{}", files.size(), prNumber);
            return files;

        } catch (WebClientResponseException e) {
            log.error("GitHub API error fetching PR files — status: {}, message: {}",
                    e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("Repo or PR not found: {}/pull/{}", repoFullName, prNumber);
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("GitHub token is invalid or expired");
            }
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Unexpected error fetching PR files: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getFileContent(String repoFullName,
            String filePath,
            String ref) {
        log.info("Fetching file content — repo: {}, file: {}, ref: {}",
                repoFullName, filePath, ref);

        try {
            Map<String, Object> response = githubWebClient.get()
                    // .uri("/repos/{repo}/contents/{path}?ref={ref}",
                    //         repoFullName, filePath, ref)
                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={ref}",
                        repoFullName.split("/")[0],
                        repoFullName.split("/")[1],
                        filePath, ref) //updated
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (response == null || !response.containsKey("content")) {
                log.warn("No content found for file: {}", filePath);
                return "";
            }

            String encodedContent = (String) response.get("content");
            // GitHub returns base64 encoded content with newlines
            String cleanedContent = encodedContent.replaceAll("\\s", "");
            byte[] decodedBytes = Base64.getDecoder().decode(cleanedContent);
            return new String(decodedBytes);

        } catch (WebClientResponseException e) {
            log.error("GitHub API error fetching file content — status: {}, file: {}",
                    e.getStatusCode(), filePath);
            return "";

        } catch (Exception e) {
            log.error("Unexpected error fetching file content: {}", e.getMessage());
            return "";
        }
    }
}