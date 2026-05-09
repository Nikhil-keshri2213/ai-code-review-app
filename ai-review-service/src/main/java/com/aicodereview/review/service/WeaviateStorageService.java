package com.aicodereview.review.service;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeaviateStorageService {

    private final WeaviateClient weaviateClient;

    public void storeCodeSnippet(
            String content,
            String fileName,
            String repoFullName,
            String language,
            Integer prNumber,
            float[] vector) {

        try {

            HashMap<String, Object> props = new HashMap<>();
            props.put("content", content);
            props.put("fileName", fileName);
            props.put("repoFullName", repoFullName);
            props.put("language", language);
            props.put("prNumber", prNumber);

            Result<WeaviateObject> result = weaviateClient.data()
                    .creator()
                    .withClassName("CodeSnippet")
                    .withProperties(props)
                    .withVector(toFloatArray(vector))
                    .run();

            if (result.hasErrors()) {
                log.error("Failed to store snippet {}: {}",
                        fileName,
                        result.getError());
            }

        } catch (Exception e) {
            log.error("Error storing snippet {}: {}",
                    fileName,
                    e.getMessage());
        }
    }

    public boolean isRepoIndexed(String repoFullName) {

        try {

            Result<List<WeaviateObject>> result = weaviateClient.data()
                    .objectsGetter()
                    .withClassName("CodeSnippet")
                    .withLimit(1)
                    .run();

            if (result.hasErrors() || result.getResult() == null) {
                return false;
            }

            for (WeaviateObject obj : result.getResult()) {

                if (obj.getProperties() != null) {

                    Object repo = obj.getProperties().get("repoFullName");

                    if (repoFullName.equals(repo)) {
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking repo index: {}",e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> findSimilarSnippets(
            float[] vector,
            String repoFullName,
            int limit) {

        try {

            Float[] floatVector = toFloatArray(vector);

            NearVectorArgument nearVector = NearVectorArgument.builder()
                    .vector(floatVector)
                    .certainty(0.7f)
                    .build();

            WhereFilter whereFilter = WhereFilter.builder()
                    .path(new String[] { "repoFullName" })
                    .operator(Operator.Equal)
                    .valueText(repoFullName)
                    .build();

            Field contentField = Field.builder().name("content").build();

            Field fileNameField = Field.builder().name("fileName").build();

            Field languageField = Field.builder().name("language").build();

            Result<GraphQLResponse> result = weaviateClient.graphQL()
                    .get()
                    .withClassName("CodeSnippet")
                    .withFields(
                            contentField,
                            fileNameField,
                            languageField)
                    .withNearVector(nearVector)
                    .withWhere(whereFilter)
                    .withLimit(limit)
                    .run();

            if (result.hasErrors() || result.getResult() == null) {

                log.warn("Weaviate query error: {}",
                        result.getError());

                return List.of();
            }

            Object data = result.getResult().getData();

            if (data == null) {
                return List.of();
            }

            List<Map<String, Object>> snippets = new ArrayList<>();

            try {

                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;

                @SuppressWarnings("unchecked")
                Map<String, Object> getMap = (Map<String, Object>) dataMap.get("Get");

                if (getMap == null) {
                    return List.of();
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> codeSnippets = (List<Map<String, Object>>) getMap.get("CodeSnippet");

                if (codeSnippets != null) {
                    snippets.addAll(codeSnippets);
                }

            } catch (Exception e) {

                log.warn("Failed to parse Weaviate response: {}",
                        e.getMessage());
            }

            log.info("Found {} similar snippets for repo: {}",
                    snippets.size(),
                    repoFullName);

            return snippets;

        } catch (Exception e) {

            log.error("Error querying similar snippets: {}",
                    e.getMessage());

            return List.of();
        }
    }

    private Float[] toFloatArray(float[] arr) {

        Float[] result = new Float[arr.length];

        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }

        return result;
    }
}