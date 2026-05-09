package com.aicodereview.fetch.service;

import com.aicodereview.fetch.client.GitHubApiClient;
import com.aicodereview.fetch.dto.DiffResult;
import com.aicodereview.fetch.dto.GitHubPRFile;
import com.aicodereview.fetch.util.CodeChunker;
import com.aicodereview.fetch.util.DiffParser;
import com.aicodereview.fetch.util.LanguageDetector;
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

        List<GitHubPRFile> files = gitHubApiClient
                .getPullRequestFiles(event.getRepoFullName(), event.getPrNumber());

        if (files.isEmpty()) {
            log.warn("No files found for PR#{} in repo: {}",
                    event.getPrNumber(), event.getRepoFullName());
            return;
        }

        log.info("Fetched {} total files for PR#{}", files.size(), event.getPrNumber());

        List<GitHubPRFile> processableFiles = files.stream()
                .filter(GitHubPRFile::isModifiedOrAdded)
                .filter(f -> !isBinaryFile(f.getFilename()))
                .toList();

        log.info("Processing {}/{} files after filtering (skipped {} binary/removed)",
                processableFiles.size(), files.size(),
                files.size() - processableFiles.size());

        for (GitHubPRFile file : processableFiles) {
            try {
                processFile(event, file);
            } catch (Exception e) {
                log.error("Failed to process file: {} — error: {}",
                        file.getFilename(), e.getMessage());
            }
        }
    }

    private void processFile(PullRequestEvent event, GitHubPRFile file) {
        String fileContent = gitHubApiClient.getFileContent(
                event.getRepoFullName(),
                file.getFilename(),
                event.getHeadSha()
        );

        DiffResult diffResult = DiffParser.parse(file.getPatch(), file.getFilename());
        String parsedDiff = DiffParser.extractChangedCode(diffResult);

        log.info("Parsed diff for {} — +{} lines, -{} lines, {} hunks",
                file.getFilename(),
                diffResult.getTotalAdditions(),
                diffResult.getTotalDeletions(),
                diffResult.getHunks().size());

        if (CodeChunker.needsChunking(fileContent)) {
            publishWithChunking(event, file, fileContent, parsedDiff);
        } else {
            ReviewRequest request = buildReviewRequest(
                    event, file, fileContent, parsedDiff, 0, 1, false);
            publishToKafka(request);
        }
    }

    private void publishWithChunking(PullRequestEvent event, GitHubPRFile file,
                                      String fileContent, String parsedDiff) {
        List<String> chunks = CodeChunker.chunk(fileContent);
        int totalChunks = chunks.size();

        log.info("File {} is large — splitting into {} chunks", file.getFilename(), totalChunks);

        for (int i = 0; i < totalChunks; i++) {
            ReviewRequest request = buildReviewRequest(
                    event, file, chunks.get(i), parsedDiff, i, totalChunks, true);
            publishToKafka(request);
        }
    }

    private ReviewRequest buildReviewRequest(PullRequestEvent event, GitHubPRFile file,
                                              String fileContent, String diffContent,
                                              int chunkIndex, int totalChunks,
                                              boolean isChunked) {
        return ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .correlationId(event.getCorrelationId())
                .repoFullName(event.getRepoFullName())
                .prNumber(event.getPrNumber())
                .fileName(file.getFilename())
                .fileContent(fileContent)
                .diffContent(diffContent)
                .language(LanguageDetector.detect(file.getFilename()))
                .headSha(event.getHeadSha())
                .senderLogin(event.getSenderLogin())
                .chunkIndex(chunkIndex)
                .totalChunks(totalChunks)
                .isChunked(isChunked)
                .build();
    }

    private void publishToKafka(ReviewRequest request) {
        String messageKey = request.isChunked()
                ? request.getFileName() + "-chunk-" + request.getChunkIndex()
                : request.getFileName();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(codeAnalysisTasksTopic, messageKey, request);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published ReviewRequest — file: {} chunk: {}/{}, " +
                                "topic: {}, partition: {}, offset: {}",
                        request.getFileName(),
                        request.getChunkIndex() + 1,
                        request.getTotalChunks(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("Failed to publish ReviewRequest for file: {} chunk: {}/{} — error: {}",
                        request.getFileName(),
                        request.getChunkIndex() + 1,
                        request.getTotalChunks(),
                        ex.getMessage());
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