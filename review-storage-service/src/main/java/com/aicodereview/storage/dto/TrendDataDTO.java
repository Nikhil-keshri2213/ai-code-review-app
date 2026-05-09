package com.aicodereview.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {
    private String weekLabel;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private long totalCount;
}