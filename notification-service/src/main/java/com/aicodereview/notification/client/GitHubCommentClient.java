package com.aicodereview.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GitHubCommentClient {

    private final WebClient webClient;

    public GitHubCommentClient(
            @Value("${github.api.base-url}") String baseUrl,
            @Value("${github.token}") String token) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public WebClient getClient() {
        return webClient;
    }
}