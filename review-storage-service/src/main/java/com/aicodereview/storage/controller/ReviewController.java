package com.aicodereview.storage.controller;

import com.aicodereview.common.dto.ApiResponse;
import com.aicodereview.storage.dto.ReviewResponseDTO;
import com.aicodereview.storage.dto.ReviewSummaryDTO;
import com.aicodereview.storage.service.ReviewStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewStorageService reviewStorageService;

    @GetMapping("/{owner}/{repo}/pulls/{prNumber}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByPR(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {

        String repoFullName = owner + "/" + repo;
        log.info("GET reviews — repo: {}, PR#{}", repoFullName, prNumber);

        List<ReviewResponseDTO> reviews =
                reviewStorageService.getReviewsByPR(repoFullName, prNumber);

        return ResponseEntity.ok(ApiResponse.success(
                "Found " + reviews.size() + " reviews", reviews));
    }

    @GetMapping("/{owner}/{repo}/pulls/{prNumber}/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> getSummary(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {

        String repoFullName = owner + "/" + repo;
        log.info("GET summary — repo: {}, PR#{}", repoFullName, prNumber);

        ReviewSummaryDTO summary =
                reviewStorageService.getSummaryByPR(repoFullName, prNumber);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/{owner}/{repo}/history")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDTO>>> getHistory(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        String repoFullName = owner + "/" + repo;
        log.info("GET history — repo: {}, page: {}, size: {}",
                repoFullName, page, size);

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ReviewResponseDTO> history =
                reviewStorageService.getHistory(repoFullName, pageable);

        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{owner}/{repo}/languages")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getLanguageStats(
            @PathVariable String owner,
            @PathVariable String repo) {

        String repoFullName = owner + "/" + repo;
        log.info("GET language stats — repo: {}", repoFullName);

        Map<String, Long> stats =
                reviewStorageService.getLanguageStats(repoFullName);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}