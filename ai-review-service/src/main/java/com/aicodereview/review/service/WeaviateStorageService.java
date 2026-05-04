package com.aicodereview.review.service;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeaviateStorageService {

    private final WeaviateClient weaviateClient;

    public void storeCodeSnippet(String content, String fileName,
                                  String repoFullName, String language,
                                  Integer prNumber, float[] vector) {
        try {
            Map<String, Object> props = new HashMap<>();
            props.put("content", content);
            props.put("fileName", fileName);
            props.put("repoFullName", repoFullName);
            props.put("language", language);
            props.put("prNumber", prNumber);

            var result = weaviateClient.data().creator()
                    .withClassName("CodeSnippet")
                    .withProperties(props)
                    .withVector(toFloatArray(vector))
                    .run();

            if (result.hasErrors()) {
                log.error("Failed to store snippet {}: {}", fileName, result.getError());
            }
        } catch (Exception e) {
            log.error("Error storing snippet {}: {}", fileName, e.getMessage());
        }
    }

    public boolean isRepoIndexed(String repoFullName) {
        try {
            var result = weaviateClient.data().objectsGetter()
                    .withClassName("CodeSnippet")
                    .withLimit(1)
                    .run();

            if (result.hasErrors() || result.getResult() == null) return false;

            // Check if any object belongs to this repo
            for (WeaviateObject obj : result.getResult()) {
                if (obj.getProperties() != null) {
                    Object repo = obj.getProperties().get("repoFullName");
                    if (repoFullName.equals(repo)) return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking repo index: {}", e.getMessage());
            return false;
        }
    }

    public long countSnippets(String repoFullName) {
        try {
            var result = weaviateClient.graphQL().aggregate()
                    .withClassName("CodeSnippet")
                    .withFields(io.weaviate.client.v1.graphql.query.fields.Field.builder()
                            .name("meta { count }")
                            .build())
                    .withWhere(WhereFilter.builder()
                            .path(new String[]{"repoFullName"})
                            .operator(Operator.Equal)
                            .valueText(repoFullName)
                            .build())
                    .run();

            if (!result.hasErrors() && result.getResult() != null) {
                log.debug("Weaviate aggregate result: {}", result.getResult());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private Float[] toFloatArray(float[] arr) {
        Float[] result = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) result[i] = arr[i];
        return result;
    }
}