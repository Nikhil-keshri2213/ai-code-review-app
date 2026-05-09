package com.aicodereview.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private String repository;
    private long totalPRs;
    private long totalIssues;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private double avgIssuesPerPR;
    private String topIssueCategory;
}