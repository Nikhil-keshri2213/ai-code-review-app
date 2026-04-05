package com.aicodereview.fetch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffLine {

    public enum Type {
        ADD, REMOVE, CONTEXT
    }

    private Type type;
    private String content;
    private int lineNumber;
}