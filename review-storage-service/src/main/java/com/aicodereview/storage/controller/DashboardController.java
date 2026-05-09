package com.aicodereview.storage.controller;

import com.aicodereview.common.dto.ApiResponse;
import com.aicodereview.storage.dto.DashboardStatsDTO;
import com.aicodereview.storage.dto.DeveloperStatsDTO;
import com.aicodereview.storage.dto.TrendDataDTO;
import com.aicodereview.storage.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{owner}/{repo}/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats(
            @PathVariable String owner,
            @PathVariable String repo) {

        String repoFullName = owner + "/" + repo;
        log.info("GET dashboard stats — repo: {}", repoFullName);

        DashboardStatsDTO stats = dashboardService.getRepoStats(repoFullName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{owner}/{repo}/trends")
    public ResponseEntity<ApiResponse<List<TrendDataDTO>>> getTrends(
            @PathVariable String owner,
            @PathVariable String repo) {

        String repoFullName = owner + "/" + repo;
        log.info("GET trends — repo: {}", repoFullName);

        List<TrendDataDTO> trends = dashboardService.getTrends(repoFullName);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/{owner}/{repo}/developers")
    public ResponseEntity<ApiResponse<List<DeveloperStatsDTO>>> getDevelopers(
            @PathVariable String owner,
            @PathVariable String repo) {

        String repoFullName = owner + "/" + repo;
        log.info("GET developer stats — repo: {}", repoFullName);

        List<DeveloperStatsDTO> devStats =
                dashboardService.getDeveloperStats(repoFullName);
        return ResponseEntity.ok(ApiResponse.success(devStats));
    }
}