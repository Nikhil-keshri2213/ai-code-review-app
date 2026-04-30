package com.aicodereview.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {

    private String repository;
    private Integer prNumber;
    private long totalIssues;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private String overallRisk;
}