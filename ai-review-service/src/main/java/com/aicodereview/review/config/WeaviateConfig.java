package com.aicodereview.review.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class WeaviateConfig {

    private WeaviateClient client;

    @Bean
    public WeaviateClient weaviateClient() {
        Config config = new Config("http", "localhost:8090");
        this.client = new WeaviateClient(config);
        return this.client;
    }

    @PostConstruct
    public void initSchema() {
        try {
            Config config = new Config("http", "localhost:8090");
            WeaviateClient weaviateClient = new WeaviateClient(config);

            // Get ALL classes and check if CodeSnippet exists
            var result = weaviateClient.schema().getter().run();

            boolean exists = false;
            if (!result.hasErrors() && result.getResult() != null) {
                var classes = result.getResult().getClasses();
                if (classes != null) {
                    exists = classes.stream()
                            .anyMatch(c -> "CodeSnippet".equals(c.getClassName()));
                }
            }

            if (!exists) {
                log.info("Creating CodeSnippet schema...");
                WeaviateClass codeSnippetClass = WeaviateClass.builder()
                        .className("CodeSnippet")
                        .description("Code snippets from GitHub PRs for RAG")
                        .vectorizer("none")
                        .properties(List.of(
                                Property.builder()
                                        .name("content")
                                        .dataType(List.of(DataType.TEXT))
                                        .build(),
                                Property.builder()
                                        .name("fileName")
                                        .dataType(List.of(DataType.TEXT))
                                        .build(),
                                Property.builder()
                                        .name("repoFullName")
                                        .dataType(List.of(DataType.TEXT))
                                        .build(),
                                Property.builder()
                                        .name("language")
                                        .dataType(List.of(DataType.TEXT))
                                        .build(),
                                Property.builder()
                                        .name("prNumber")
                                        .dataType(List.of(DataType.INT))
                                        .build()))
                        .build();

                var createResult = weaviateClient.schema()
                        .classCreator()
                        .withClass(codeSnippetClass)
                        .run();

                if (createResult.hasErrors()) {
                    log.error("❌ Schema creation failed: {}", createResult.getError());
                } else {
                    log.info("✅ Weaviate CodeSnippet schema created!");
                }
            } else {
                log.info("✅ Weaviate CodeSnippet schema already exists");
            }
        } catch (Exception e) {
            log.error("❌ Weaviate schema init error: {}", e.getMessage());
        }
    }
}