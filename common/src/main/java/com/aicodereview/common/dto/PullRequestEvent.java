package com.aicodereview.common.dto;

import lombok.Data;

@Data
public class PullRequestEvent {

    private String repository;
    private String branch;
    private String commitId;
    private String author;
    private String pullRequestId;

}