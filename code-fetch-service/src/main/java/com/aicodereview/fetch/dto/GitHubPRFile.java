package com.aicodereview.fetch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPRFile {

    private String sha;
    private String filename;
    private String status;
    private int additions;
    private int deletions;
    private int changes;
    private String patch;

    @JsonProperty("raw_url")
    private String rawUrl;

    @JsonProperty("contents_url")
    private String contentsUrl;

    @JsonProperty("blob_url")
    private String blobUrl;

    public boolean isModifiedOrAdded() {
        return "added".equals(status) || "modified".equals(status);
    }

    public String detectLanguage() {
        if (filename == null) return "unknown";
        if (filename.endsWith(".java"))   return "java";
        if (filename.endsWith(".py"))     return "python";
        if (filename.endsWith(".js"))     return "javascript";
        if (filename.endsWith(".ts"))     return "typescript";
        if (filename.endsWith(".go"))     return "go";
        if (filename.endsWith(".rb"))     return "ruby";
        if (filename.endsWith(".cs"))     return "csharp";
        if (filename.endsWith(".cpp"))    return "cpp";
        if (filename.endsWith(".php"))    return "php";
        if (filename.endsWith(".yaml") || filename.endsWith(".yml")) return "yaml";
        if (filename.endsWith(".sql"))    return "sql";
        if (filename.endsWith(".xml"))    return "xml";
        if (filename.endsWith(".json"))   return "json";
        return "unknown";
    }
}