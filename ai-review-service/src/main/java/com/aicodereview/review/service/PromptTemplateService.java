package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptTemplateService {

    private static final int MAX_CONTENT_CHARS = 8000;

    // ── Language-specific rules ──
    private static final Map<String, String> LANGUAGE_RULES = Map.of(
            "java",
            """
            Java-specific rules:
            - Check for NullPointerException risks (unguarded null dereferences)
            - Verify exceptions are caught or declared
            - Check resource leaks (streams, connections not closed)
            - Flag hardcoded credentials or secrets
            - Check for SQL injection if DB queries present
            - Verify proper use of generics and collections
            """,
            "python",
            """
            Python-specific rules:
            - Check PEP8 style violations (naming, spacing)
            - Flag missing type hints on public functions
            - Check for bare except clauses
            - Flag mutable default arguments in function signatures
            - Check for SQL injection in string-formatted queries
            - Verify proper use of context managers (with statements)
            """,
            "javascript",
            """
            JavaScript-specific rules:
            - Check for missing async/await error handling
            - Flag undefined/null dereferences
            - Check for == instead of === comparisons
            - Flag var usage (prefer const/let)
            - Check for promise chains without .catch()
            - Flag eval() usage or innerHTML with untrusted data
            """,
            "typescript",
            """
            TypeScript-specific rules:
            - Flag use of 'any' type
            - Check for unhandled promise rejections
            - Verify proper null/undefined checks
            - Flag missing return types on public functions
            - Check for type assertions that bypass safety
            """,
            "sql",
            """
            SQL-specific rules:
            - Check for SQL injection vulnerabilities
            - Flag SELECT * usage in production code
            - Check for missing WHERE clauses in UPDATE/DELETE
            - Flag missing indexes on join columns
            """
    );

    public String buildSystemPrompt(String language) {
        String langRules = LANGUAGE_RULES.getOrDefault(
                language != null ? language.toLowerCase() : "",
                "- Check for common bugs, security issues, performance problems and code style violations."
        );

        return """
                You are an expert code reviewer with deep knowledge of software engineering best practices, security vulnerabilities, and performance optimization.

                Your task is to review the provided code and identify issues.

                SEVERITY LEVELS:
                - HIGH: Security vulnerabilities, crashes, data loss risks, SQL injection, hardcoded secrets
                - MEDIUM: Bugs, logic errors, performance issues, resource leaks, unhandled exceptions
                - LOW: Code style, naming conventions, missing documentation, minor improvements

                CATEGORIES:
                - SECURITY: Authentication, injection, exposure of sensitive data
                - BUG: Logic errors, incorrect behavior, crashes
                - PERFORMANCE: Inefficient algorithms, unnecessary DB calls, memory issues
                - CODE_STYLE: Naming, formatting, readability
                - MAINTAINABILITY: Code duplication, complexity, missing tests
                - OTHER: Anything that doesn't fit above

                %s

                OUTPUT FORMAT (strictly follow this):
                Return ONLY a valid JSON array. No explanation before or after. No markdown code blocks.
                Each issue must have ALL these fields:
                [
                  {
                    "fileName": "exact filename",
                    "lineNumber": <integer or null>,
                    "severity": "HIGH" | "MEDIUM" | "LOW",
                    "category": "SECURITY" | "BUG" | "PERFORMANCE" | "CODE_STYLE" | "MAINTAINABILITY" | "OTHER",
                    "comment": "clear description of the issue",
                    "suggestion": "specific fix or improvement"
                  }
                ]

                If no issues found, return empty array: []
                Do NOT wrap in markdown. Return raw JSON only.
                """.formatted(langRules);
    }

    public String buildUserPrompt(ReviewRequest request) {
        String fileName = request.getFileName() != null ? request.getFileName() : "unknown";
        String language = request.getLanguage() != null ? request.getLanguage() : "unknown";
        String diff = request.getDiffContent() != null ? request.getDiffContent() : "";
        String content = request.getFileContent() != null ? request.getFileContent() : "";

        // Truncate file content to stay within token limits
        if (content.length() > MAX_CONTENT_CHARS) {
            content = content.substring(0, MAX_CONTENT_CHARS)
                    + "\n... [truncated - file too large] ...";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("File: ").append(fileName).append("\n");
        prompt.append("Language: ").append(language).append("\n");

        if (request.isChunked()) {
            prompt.append("Chunk: ").append(request.getChunkIndex() + 1)
                  .append("/").append(request.getTotalChunks()).append("\n");
        }

        prompt.append("\n");

        if (!diff.isBlank()) {
            prompt.append("=== CHANGED CODE (diff) ===\n");
            prompt.append(diff).append("\n\n");
        }

        if (!content.isBlank()) {
            prompt.append("=== FULL FILE CONTENT ===\n");
            prompt.append(content).append("\n");
        }

        if (diff.isBlank() && content.isBlank()) {
            prompt.append("No code content available for review.\n");
        }

        prompt.append("\nReview the code above and return JSON array of issues found.");

        return prompt.toString();
    }
}