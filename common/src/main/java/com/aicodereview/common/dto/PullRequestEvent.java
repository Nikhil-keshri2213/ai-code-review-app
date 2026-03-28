package com.aicodereview.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestEvent {

    private String action;

    @JsonProperty("number")
    private Integer prNumber;

    @JsonProperty("pull_request")
    private PullRequestDetail pullRequest;

    private Repository repository;

    private Sender sender;

    // ── Nested: pull_request object ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestDetail {
        private String title;
        private String state;

        @JsonProperty("head")
        private BranchInfo head;

        @JsonProperty("base")
        private BranchInfo base;
    }

    // ── Nested: head/base branch ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchInfo {
        private String ref;
        private String sha;
    }

    // ── Nested: repository object ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private Long id;
        private String name;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("private")
        private Boolean isPrivate;
    }

    // ── Nested: sender object ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sender {
        private String login;
        private Long id;
    }

    // ── Helper methods ──
    public String getRepoFullName() {
        return repository != null ? repository.getFullName() : null;
    }

    public String getHeadSha() {
        return pullRequest != null && pullRequest.getHead() != null
                ? pullRequest.getHead().getSha() : null;
    }

    public String getHeadRef() {
        return pullRequest != null && pullRequest.getHead() != null
                ? pullRequest.getHead().getRef() : null;
    }

    public String getBaseRef() {
        return pullRequest != null && pullRequest.getBase() != null
                ? pullRequest.getBase().getRef() : null;
    }

    public String getPrTitle() {
        return pullRequest != null ? pullRequest.getTitle() : null;
    }

    public String getSenderLogin() {
        return sender != null ? sender.getLogin() : null;
    }
}