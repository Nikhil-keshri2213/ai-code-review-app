package com.aicodereview.fetch.service;

import com.aicodereview.fetch.client.GitHubApiClient;
import com.aicodereview.fetch.dto.DiffResult;
import com.aicodereview.fetch.dto.GitHubPRFile;
import com.aicodereview.fetch.util.DiffParser;
import com.aicodereview.common.dto.PullRequestEvent;
import com.aicodereview.common.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeFetchService {

    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "svg", "ico", "bmp", "webp",
            "pdf", "zip", "tar", "gz", "jar", "war", "class",
            "exe", "dll", "so", "dylib", "mp3", "mp4", "mov"
    );

    private final GitHubApiClient gitHubApiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.code-analysis-tasks}")
    private String codeAnalysisTasksTopic;

    public void fetchAndPublish(PullRequestEvent event) {
        log.info("Starting file fetch — repo: {}, PR#: {}, correlationId: {}",
                event.getRepoFullName(), event.getPrNumber(), event.getCorrelationId());

        // Step 1 — fetch changed files from GitHub
        List<GitHubPRFile> files = gitHubApiClient
                .getPullRequestFiles(event.getRepoFullName(), event.getPrNumber());

        if (files.isEmpty()) {
            log.warn("No files found for PR#{} in repo: {}",
                    event.getPrNumber(), event.getRepoFullName());
            return;
        }

        log.info("Fetched {} total files for PR#{}", files.size(), event.getPrNumber());

        // Step 2 — filter to processable files
        List<GitHubPRFile> processableFiles = files.stream()
                .filter(GitHubPRFile::isModifiedOrAdded)
                .filter(f -> !isBinaryFile(f.getFilename()))
                .toList();

        log.info("Processing {}/{} files after filtering (skipped {} binary/removed)",
                processableFiles.size(), files.size(),
                files.size() - processableFiles.size());

        // Step 3 — build and publish ReviewRequest per file
        for (GitHubPRFile file : processableFiles) {
            try {
                ReviewRequest request = buildReviewRequest(event, file);
                publishToKafka(request);
            } catch (Exception e) {
                log.error("Failed to process file: {} — error: {}",
                        file.getFilename(), e.getMessage());
            }
        }
    }

    private ReviewRequest buildReviewRequest(PullRequestEvent event, GitHubPRFile file) {
        // Fetch full file content
        String fileContent = gitHubApiClient.getFileContent(
                event.getRepoFullName(),
                file.getFilename(),
                event.getHeadSha()
        );

        // Parse the diff patch into structured format
        DiffResult diffResult = DiffParser.parse(file.getPatch(), file.getFilename());
        String parsedDiff = DiffParser.extractChangedCode(diffResult);

        log.info("Parsed diff for {} — +{} lines, -{} lines, {} hunks",
                file.getFilename(),
                diffResult.getTotalAdditions(),
                diffResult.getTotalDeletions(),
                diffResult.getHunks().size()
        );

        return ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .correlationId(event.getCorrelationId())
                .repoFullName(event.getRepoFullName())
                .prNumber(event.getPrNumber())
                .fileName(file.getFilename())
                .fileContent(fileContent)
                .diffContent(parsedDiff)
                .language(file.detectLanguage())
                .headSha(event.getHeadSha())
                .senderLogin(event.getSenderLogin())
                .build();
        }

    private void publishToKafka(ReviewRequest request) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        codeAnalysisTasksTopic,
                        request.getFileName(),
                        request
                );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published ReviewRequest — file: {}, topic: {}, partition: {}, offset: {}",
                        request.getFileName(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("Failed to publish ReviewRequest for file: {} — error: {}",
                        request.getFileName(), ex.getMessage());
            }
        });
    }

    private boolean isBinaryFile(String filename) {
        if (filename == null) return false;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return false;
        String ext = filename.substring(lastDot + 1).toLowerCase();
        return BINARY_EXTENSIONS.contains(ext);
    }
}