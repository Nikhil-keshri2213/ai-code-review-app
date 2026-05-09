package com.aicodereview.review.util;

public record LanguageInfo(
        String language,
        String displayName,
        String lintTool,
        String fileExtension
) {
    public static LanguageInfo of(String ext) {
        return switch (ext.toLowerCase()) {
            case ".java"       -> new LanguageInfo("java",       "Java",        "Checkstyle",   ext);
            case ".py"         -> new LanguageInfo("python",     "Python",      "PEP8/flake8",  ext);
            case ".js"         -> new LanguageInfo("javascript", "JavaScript",  "ESLint",        ext);
            case ".ts"         -> new LanguageInfo("typescript", "TypeScript",  "ESLint+TSLint", ext);
            case ".sql"        -> new LanguageInfo("sql",        "SQL",         "sqlfluff",      ext);
            case ".go"         -> new LanguageInfo("go",         "Go",          "golint",        ext);
            case ".kt"         -> new LanguageInfo("kotlin",     "Kotlin",      "ktlint",        ext);
            case ".rs"         -> new LanguageInfo("rust",       "Rust",        "clippy",        ext);
            case ".sh"         -> new LanguageInfo("shell",      "Shell",       "shellcheck",    ext);
            case ".cpp",".cc"  -> new LanguageInfo("cpp",        "C++",         "clang-tidy",    ext);
            case ".cs"         -> new LanguageInfo("csharp",     "C#",          "Roslyn",        ext);
            default            -> new LanguageInfo("unknown",    "Unknown",     "none",          ext);
        };
    }
}