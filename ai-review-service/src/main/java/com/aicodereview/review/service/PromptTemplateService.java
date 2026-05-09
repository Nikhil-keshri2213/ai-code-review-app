package com.aicodereview.review.service;

import com.aicodereview.common.dto.ReviewRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptTemplateService {

        private static final int MAX_CONTENT_CHARS = 8000;

        // ── Language-specific rules ──
        private static final Map<String, String> LANGUAGE_RULES = Map.ofEntries(
                        Map.entry("java", """
                                        Java-specific rules (Checkstyle):
                                        - Check for NullPointerException risks (unguarded null dereferences)
                                        - Verify exceptions are caught or declared — no swallowed exceptions
                                        - Check resource leaks (streams, connections not in try-with-resources)
                                        - Flag hardcoded credentials or secrets
                                        - Check for SQL injection if DB queries present
                                        - Verify proper use of generics and collections
                                        - Flag missing @Override annotations
                                        - Check for == on String comparisons instead of .equals()
                                        """),
                        Map.entry("python", """
                                        Python-specific rules (PEP8):
                                        - Check PEP8 style violations (naming, spacing, line length)
                                        - Flag missing type hints on public functions
                                        - Check for bare except clauses — catch specific exceptions
                                        - Flag mutable default arguments in function signatures
                                        - Check for SQL injection in string-formatted queries
                                        - Verify proper use of context managers (with statements)
                                        - Flag global variable usage
                                        - Check for dangerous eval() or exec() calls
                                        """),
                        Map.entry("javascript", """
                                        JavaScript-specific rules (ESLint):
                                        - Check for missing async/await error handling
                                        - Flag undefined/null dereferences without checks
                                        - Check for == instead of === comparisons
                                        - Flag var usage (prefer const/let)
                                        - Check for promise chains without .catch()
                                        - Flag eval() usage or innerHTML with untrusted data
                                        - Check for console.log left in production code
                                        - Flag prototype pollution vulnerabilities
                                        """),
                        Map.entry("typescript", """
                                        TypeScript-specific rules (ESLint + TSLint):
                                        - Flag use of 'any' type — use proper types
                                        - Check for unhandled promise rejections
                                        - Verify proper null/undefined checks with optional chaining
                                        - Flag missing return types on public functions
                                        - Check for type assertions (as Type) that bypass safety
                                        - Flag non-null assertions (!) without justification
                                        """),
                        Map.entry("sql", """
                                        SQL-specific rules:
                                        - Check for SQL injection vulnerabilities
                                        - Flag SELECT * usage in production code
                                        - Check for missing WHERE clauses in UPDATE/DELETE
                                        - Flag missing indexes on join columns
                                        - Check for N+1 query patterns
                                        - Flag missing transactions on multi-statement operations
                                        """),
                        Map.entry("go", """
                                        Go-specific rules (golint/staticcheck):
                                        - Check for goroutine leaks (goroutines without proper exit)
                                        - Verify errors are checked — never ignore err returns
                                        - Flag missing context.Context propagation
                                        - Check for data races in concurrent code
                                        - Flag defer in loops (performance issue)
                                        - Verify proper use of sync primitives (Mutex, WaitGroup)
                                        - Check for panic() in non-exceptional paths
                                        """),
                        Map.entry("kotlin", """
                                        Kotlin-specific rules:
                                        - Flag unnecessary null checks on non-nullable types
                                        - Check for !! (non-null assertion) without justification
                                        - Verify coroutine scope lifecycle management
                                        - Flag blocking calls inside suspend functions
                                        - Check for improper use of lateinit var
                                        - Flag mutable state in shared contexts (thread safety)
                                        """),
                        Map.entry("rust", """
                                        Rust-specific rules (clippy):
                                        - Flag unwrap() and expect() without justification — prefer ?
                                        - Check for unnecessary clones (performance)
                                        - Verify lifetime annotations are correct
                                        - Flag unsafe blocks without comments explaining safety
                                        - Check for integer overflow in arithmetic
                                        - Flag unused Result returns (must_use)
                                        """),
                        Map.entry("shell", """
                                        Shell script rules (shellcheck):
                                        - Check for missing set -e (exit on error)
                                        - Flag unquoted variables (word splitting/globbing)
                                        - Check for command injection via eval or unquoted input
                                        - Flag use of deprecated backtick syntax — use $()
                                        - Check for missing error handling after critical commands
                                        - Flag hardcoded passwords or tokens
                                        """),
                        Map.entry("cpp", """
                                        C++ rules:
                                        - Check for memory leaks (raw new without delete or RAII)
                                        - Flag buffer overflows (raw array access without bounds)
                                        - Check for use-after-free patterns
                                        - Verify RAII patterns for resource management
                                        - Flag missing virtual destructors in base classes
                                        - Check for unsafe casts (C-style casts vs static_cast)
                                        """));

        public String buildSystemPrompt(String language) {
                String langRules = LANGUAGE_RULES.getOrDefault(
                                language != null ? language.toLowerCase() : "",
                                "- Check for common bugs, security issues, performance problems and code style violations.");

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
                                """
                                .formatted(langRules);
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

        public String buildSystemPrompt(String language, String ragContext) {
                String langRules = LANGUAGE_RULES.getOrDefault(
                                language != null ? language.toLowerCase() : "",
                                "- Check for common bugs, security issues, performance problems and code style violations.");

                String ragSection = (ragContext != null && !ragContext.isBlank())
                                ? ragContext + "\n---\n\n"
                                : "";

                return """
                                %sYou are an expert code reviewer with deep knowledge of software engineering best practices, security vulnerabilities, and performance optimization.

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
                                """
                                .formatted(ragSection, langRules);
        }
}