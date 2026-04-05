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
public class DiffResult {

    private String fileName;
    private int totalAdditions;
    private int totalDeletions;
    private List<DiffHunk> hunks;
    private boolean parsedSuccessfully;
}