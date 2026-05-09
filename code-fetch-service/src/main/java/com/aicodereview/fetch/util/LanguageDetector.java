package com.aicodereview.fetch.util;

public class LanguageDetector {

    private LanguageDetector() {}

    public static String detect(String fileName) {
        if (fileName == null) return "unknown";
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".java"))             return "java";
        if (lower.endsWith(".py"))               return "python";
        if (lower.endsWith(".js"))               return "javascript";
        if (lower.endsWith(".ts"))               return "typescript";
        if (lower.endsWith(".sql"))              return "sql";
        if (lower.endsWith(".go"))               return "go";
        if (lower.endsWith(".rs"))               return "rust";
        if (lower.endsWith(".kt"))               return "kotlin";
        if (lower.endsWith(".cs"))               return "csharp";
        if (lower.endsWith(".cpp") ||
            lower.endsWith(".cc"))               return "cpp";
        if (lower.endsWith(".sh"))               return "shell";
        if (lower.endsWith(".yml") ||
            lower.endsWith(".yaml"))             return "yaml";
        if (lower.endsWith(".xml"))              return "xml";
        if (lower.endsWith(".json"))             return "json";
        if (lower.endsWith(".md"))               return "markdown";
        return "unknown";
    }
}