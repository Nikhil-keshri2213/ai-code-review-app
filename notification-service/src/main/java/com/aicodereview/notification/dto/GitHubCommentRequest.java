package com.aicodereview.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitHubCommentRequest {

    private String body;

    @JsonProperty("commit_id")
    private String commitId;

    private String path;

    private Integer line;
}