package com.aicodereview.common.dto;

import lombok.Data;

@Data
public class ReviewResult {

    private String summary;
    private String issues;
    private String suggestions;
    private String score;
}