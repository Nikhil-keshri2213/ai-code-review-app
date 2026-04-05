package com.aicodereview.fetch.util;

import com.aicodereview.fetch.dto.DiffHunk;
import com.aicodereview.fetch.dto.DiffLine;
import com.aicodereview.fetch.dto.DiffResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DiffParser {

    private static final Pattern HUNK_HEADER = Pattern.compile(
            "^@@ -\\d+(?:,\\d+)? \\+(\\d+)(?:,(\\d+))? @@.*"
    );

    private static final int CONTEXT_LINES = 3;

    private DiffParser() {}

    public static DiffResult parse(String patch, String fileName) {
        if (patch == null || patch.isBlank()) {
            log.debug("Empty or null patch for file: {}", fileName);
            return DiffResult.builder()
                    .fileName(fileName)
                    .hunks(List.of())
                    .parsedSuccessfully(false)
                    .build();
        }

        try {
            List<DiffHunk> hunks = new ArrayList<>();
            String[] lines = patch.split("\n");

            DiffHunk currentHunk = null;
            List<DiffLine> currentLines = new ArrayList<>();
            int currentLineNumber = 0;
            int totalAdditions = 0;
            int totalDeletions = 0;

            for (String rawLine : lines) {
                Matcher matcher = HUNK_HEADER.matcher(rawLine);

                if (matcher.matches()) {
                    // Save previous hunk if exists
                    if (currentHunk != null) {
                        currentHunk.setLines(currentLines);
                        currentHunk.setEndLine(currentLineNumber);
                        hunks.add(currentHunk);
                    }

                    // Start new hunk
                    int newStart = Integer.parseInt(matcher.group(1));
                    currentLineNumber = newStart;
                    currentLines = new ArrayList<>();
                    currentHunk = DiffHunk.builder()
                            .startLine(newStart)
                            .addedLines(0)
                            .removedLines(0)
                            .build();

                } else if (currentHunk != null) {
                    if (rawLine.startsWith("+")) {
                        String content = rawLine.substring(1);
                        currentLines.add(DiffLine.builder()
                                .type(DiffLine.Type.ADD)
                                .content(content)
                                .lineNumber(currentLineNumber)
                                .build());
                        currentHunk.setAddedLines(currentHunk.getAddedLines() + 1);
                        totalAdditions++;
                        currentLineNumber++;

                    } else if (rawLine.startsWith("-")) {
                        String content = rawLine.substring(1);
                        currentLines.add(DiffLine.builder()
                                .type(DiffLine.Type.REMOVE)
                                .content(content)
                                .lineNumber(0) // removed lines have no new line number
                                .build());
                        currentHunk.setRemovedLines(currentHunk.getRemovedLines() + 1);
                        totalDeletions++;

                    } else {
                        // Context line (starts with space or is empty)
                        String content = rawLine.length() > 0 ? rawLine.substring(1) : "";
                        currentLines.add(DiffLine.builder()
                                .type(DiffLine.Type.CONTEXT)
                                .content(content)
                                .lineNumber(currentLineNumber)
                                .build());
                        currentLineNumber++;
                    }
                }
            }

            // Save the last hunk
            if (currentHunk != null) {
                currentHunk.setLines(currentLines);
                currentHunk.setEndLine(currentLineNumber);
                hunks.add(currentHunk);
            }

            return DiffResult.builder()
                    .fileName(fileName)
                    .totalAdditions(totalAdditions)
                    .totalDeletions(totalDeletions)
                    .hunks(hunks)
                    .parsedSuccessfully(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse diff for file: {} — {}", fileName, e.getMessage());
            return DiffResult.builder()
                    .fileName(fileName)
                    .hunks(List.of())
                    .parsedSuccessfully(false)
                    .build();
        }
    }

    public static String extractChangedCode(DiffResult diffResult) {
        if (diffResult == null || diffResult.getHunks() == null
                || diffResult.getHunks().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (DiffHunk hunk : diffResult.getHunks()) {
            List<DiffLine> lines = hunk.getLines();
            if (lines == null || lines.isEmpty()) continue;

            sb.append("--- Changes starting at line ")
              .append(hunk.getStartLine())
              .append(" (+")
              .append(hunk.getAddedLines())
              .append(" -")
              .append(hunk.getRemovedLines())
              .append(") ---\n");

            // Find indices of ADD lines
            List<Integer> addIndices = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).getType() == DiffLine.Type.ADD) {
                    addIndices.add(i);
                }
            }

            if (addIndices.isEmpty()) continue;

            // Build set of indices to include (ADD lines + CONTEXT_LINES around them)
            java.util.Set<Integer> toInclude = new java.util.TreeSet<>();
            for (int idx : addIndices) {
                for (int c = Math.max(0, idx - CONTEXT_LINES);
                     c <= Math.min(lines.size() - 1, idx + CONTEXT_LINES); c++) {
                    toInclude.add(c);
                }
            }

            // Build output with line numbers
            int prevIdx = -2;
            for (int idx : toInclude) {
                if (idx > prevIdx + 1 && prevIdx >= 0) {
                    sb.append("...\n");
                }

                DiffLine line = lines.get(idx);
                String prefix = switch (line.getType()) {
                    case ADD     -> "+";
                    case REMOVE  -> "-";
                    case CONTEXT -> " ";
                };

                if (line.getLineNumber() > 0) {
                    sb.append(String.format("L%-4d %s %s%n",
                            line.getLineNumber(), prefix, line.getContent()));
                } else {
                    sb.append(String.format("      %s %s%n",
                            prefix, line.getContent()));
                }

                prevIdx = idx;
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}