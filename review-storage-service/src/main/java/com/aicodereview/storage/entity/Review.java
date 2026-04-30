package com.aicodereview.storage.entity;

import com.aicodereview.common.enums.ReviewCategory;
import com.aicodereview.common.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews",
    indexes = {
        @Index(name = "idx_reviews_repository", columnList = "repository"),
        @Index(name = "idx_reviews_pr_number",  columnList = "pr_number"),
        @Index(name = "idx_reviews_repo_pr",    columnList = "repository,pr_number")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String repository;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private ReviewCategory category;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "llm_provider")
    private String llmProvider;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}