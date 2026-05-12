package com.aicodereview.aireview.service;

import com.aicodereview.common.dto.ReviewRequest;
import com.aicodereview.review.service.PromptTemplateService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateServiceTest {

    private PromptTemplateService service;

    @BeforeEach
    void setUp() {
        service = new PromptTemplateService();
    }

    @Test
    void buildSystemPrompt_java_containsJavaRules() {
        String prompt = service.buildSystemPrompt("java");

        assertThat(prompt).containsIgnoringCase("NullPointerException");
        assertThat(prompt).containsIgnoringCase("Checkstyle");
    }

    @Test
    void buildSystemPrompt_python_containsPythonRules() {
        String prompt = service.buildSystemPrompt("python");

        assertThat(prompt).containsIgnoringCase("PEP8");
        assertThat(prompt).containsIgnoringCase("type hints");
    }

    @Test
    void buildSystemPrompt_null_containsDefaultRules() {
        String prompt = service.buildSystemPrompt(null);

        assertThat(prompt).containsIgnoringCase("bugs");
        assertThat(prompt).containsIgnoringCase("security");
    }

    @Test
    void buildSystemPrompt_unknown_containsDefaultRules() {
        String prompt = service.buildSystemPrompt("cobol");

        assertThat(prompt).isNotBlank();
        assertThat(prompt).containsIgnoringCase("JSON");
    }

    @Test
    void buildSystemPrompt_alwaysContainsOutputFormat() {
        String prompt = service.buildSystemPrompt("java");

        assertThat(prompt).contains("fileName");
        assertThat(prompt).contains("lineNumber");
        assertThat(prompt).contains("severity");
        assertThat(prompt).contains("HIGH");
        assertThat(prompt).contains("MEDIUM");
        assertThat(prompt).contains("LOW");
    }

    @Test
    void buildUserPrompt_includesFileName() {
        ReviewRequest request = ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .fileName("UserService.java")
                .language("java")
                .fileContent("public class UserService {}")
                .diffContent("")
                .chunkIndex(0).totalChunks(1).isChunked(false)
                .build();

        String prompt = service.buildUserPrompt(request);

        assertThat(prompt).contains("UserService.java");
        assertThat(prompt).contains("java");
    }

    @Test
    void buildUserPrompt_includesDiffContent() {
        ReviewRequest request = ReviewRequest.builder()
                .requestId(UUID.randomUUID())
                .fileName("Foo.java")
                .language("java")
                .fileContent("")
                .diffContent("+    String password = \"abc123\";")
                .chunkIndex(0).totalChunks(1).isChunked(false)
                .build();

        String prompt = service.buildUserPrompt(request);

        assertThat(prompt).contains("password");
        assertThat(prompt).contains("CHANGED CODE");
    }

    @Test
    void buildSystemPrompt_withRagContext_prependsContext() {
        String ragContext = "## Similar Code\nSome similar Java code here";
        String prompt = service.buildSystemPrompt("java", ragContext);

        assertThat(prompt).startsWith(ragContext);
    }
}