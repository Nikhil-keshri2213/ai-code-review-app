package com.aicodereview.storage.service;

import com.aicodereview.storage.dto.DashboardStatsDTO;
import com.aicodereview.storage.dto.DeveloperStatsDTO;
import com.aicodereview.storage.dto.TrendDataDTO;
import com.aicodereview.storage.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReviewRepository reviewRepository;

    public DashboardStatsDTO getRepoStats(String repo) {
        List<Object[]> results = reviewRepository.getRepoStats(repo);

        if (results == null || results.isEmpty()) {
            return DashboardStatsDTO.builder().repository(repo).build();
        }

        Object[] row = results.get(0);

        if (row == null || row.length == 0 || row[0] == null) {
            return DashboardStatsDTO.builder().repository(repo).build();
        }

        long totalPRs    = toLong(row[0]);
        long totalIssues = toLong(row[1]);
        long high        = toLong(row[2]);
        long medium      = toLong(row[3]);
        long low         = toLong(row[4]);
        double avg       = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
        String topCat    = row[6] != null ? row[6].toString() : "NONE";

        log.info("Dashboard stats for {}: PRs={}, issues={}, HIGH={}",
                repo, totalPRs, totalIssues, high);

        return DashboardStatsDTO.builder()
                .repository(repo)
                .totalPRs(totalPRs)
                .totalIssues(totalIssues)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .avgIssuesPerPR(avg)
                .topIssueCategory(topCat)
                .build();
    }

    public List<TrendDataDTO> getTrends(String repo) {
        List<Object[]> rows = reviewRepository.getTrends(repo);
        List<TrendDataDTO> trends = new ArrayList<>();

        for (Object[] row : rows) {
            trends.add(TrendDataDTO.builder()
                    .weekLabel(row[0] != null ? row[0].toString() : "")
                    .highCount(toLong(row[1]))
                    .mediumCount(toLong(row[2]))
                    .lowCount(toLong(row[3]))
                    .totalCount(toLong(row[4]))
                    .build());
        }

        log.info("Trend data for {}: {} weeks", repo, trends.size());
        return trends;
    }

    public List<DeveloperStatsDTO> getDeveloperStats(String repo) {
        List<Object[]> rows = reviewRepository.getDeveloperStats(repo);
        List<DeveloperStatsDTO> stats = new ArrayList<>();

        for (Object[] row : rows) {
            stats.add(DeveloperStatsDTO.builder()
                    .repository(row[0] != null ? row[0].toString() : repo)
                    .totalPRs(toLong(row[1]))
                    .totalIssues(toLong(row[2]))
                    .highCount(toLong(row[3]))
                    .mediumCount(toLong(row[4]))
                    .lowCount(toLong(row[5]))
                    .build());
        }

        return stats;
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number number) return number.longValue();
        try { return Long.parseLong(val.toString()); }
        catch (Exception e) { return 0L; }
    }
}