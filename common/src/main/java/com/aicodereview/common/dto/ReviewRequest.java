package com.aicodereview.common.dto;

import lombok.Data;

@Data
public class ReviewRequest {

    private String language;
    private String code;
    private String repository;
    private String filePath;

}