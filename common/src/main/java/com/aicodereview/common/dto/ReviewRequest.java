package com.aicodereview.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    private UUID requestId;
    private String repoFullName;
    private Integer prNumber;
    private String fileName;
    private String fileContent;
    private String diffContent;
    private String language;
    private String headSha;
    private String senderLogin;

    private String correlationId;

    @Builder.Default
    private int chunkIndex = 0;

    @Builder.Default
    private int totalChunks = 1;

    @Builder.Default
    private boolean isChunked = false;
}