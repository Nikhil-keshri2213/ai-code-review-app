package com.aicodereview.fetch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffHunk {

    private int startLine;
    private int endLine;
    private int addedLines;
    private int removedLines;
    private List<DiffLine> lines;
}