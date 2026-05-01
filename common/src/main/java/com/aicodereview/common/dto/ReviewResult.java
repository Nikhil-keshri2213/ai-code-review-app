package com.aicodereview.common.dto;

import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResult {

    private UUID resultId;
    private UUID requestId;
    private String repoFullName;
    private Integer prNumber;
    private String fileName;
    private String comment;
    private Severity severity;
    private ReviewCategory category;
    private Integer lineNumber;
    private String suggestion;

    private String llmProvider;

    private Double confidenceScore;

    private String commitSha;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}