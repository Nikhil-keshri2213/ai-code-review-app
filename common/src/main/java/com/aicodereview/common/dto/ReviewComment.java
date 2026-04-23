package com.aicodereview.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewComment {

    private String fileName;
    private Integer lineNumber;
    private String severity;
    private String category;
    private String comment;
    private String suggestion;
}