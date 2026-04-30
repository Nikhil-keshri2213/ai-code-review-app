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

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewStorageService reviewStorageService;

    // GET all reviews for a specific PR
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

    // GET summary for a specific PR
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

    // GET paginated history for a repo
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
}