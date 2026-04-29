package com.aicodereview.storage.dto;

import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {

    private UUID id;
    private String repository;
    private Integer prNumber;
    private String fileName;
    private String comment;
    private Severity severity;
    private ReviewCategory category;
    private Integer lineNumber;
    private String suggestion;
    private String llmProvider;
    private BigDecimal confidenceScore;
    private String status;
    private LocalDateTime createdAt;
}