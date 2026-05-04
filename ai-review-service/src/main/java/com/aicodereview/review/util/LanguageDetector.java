package com.aicodereview.review.util;

import org.springframework.stereotype.Component;

@Component
public class LanguageDetector {

    public String detect(String fileName) {
        if (fileName == null) return "UNKNOWN";
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".java"))       return "JAVA";
        if (lower.endsWith(".py"))         return "PYTHON";
        if (lower.endsWith(".js"))         return "JAVASCRIPT";
        if (lower.endsWith(".ts"))         return "TYPESCRIPT";
        if (lower.endsWith(".sql"))        return "SQL";
        if (lower.endsWith(".go"))         return "GO";
        if (lower.endsWith(".rs"))         return "RUST";
        if (lower.endsWith(".kt"))         return "KOTLIN";
        if (lower.endsWith(".cs"))         return "CSHARP";
        if (lower.endsWith(".cpp") ||
            lower.endsWith(".cc"))         return "CPP";
        if (lower.endsWith(".yml") ||
            lower.endsWith(".yaml"))       return "YAML";
        if (lower.endsWith(".xml"))        return "XML";
        if (lower.endsWith(".json"))       return "JSON";
        if (lower.endsWith(".md"))         return "MARKDOWN";
        if (lower.endsWith(".sh"))         return "SHELL";
        return "UNKNOWN";
    }

    public boolean isCodeFile(String fileName) {
        String lang = detect(fileName);
        return !lang.equals("UNKNOWN") &&
               !lang.equals("MARKDOWN") &&
               !lang.equals("YAML") &&
               !lang.equals("XML") &&
               !lang.equals("JSON");
    }
}