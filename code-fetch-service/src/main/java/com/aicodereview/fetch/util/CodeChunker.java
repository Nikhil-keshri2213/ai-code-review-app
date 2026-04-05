package com.aicodereview.fetch.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CodeChunker {

    public static final int MAX_CHARS = 12000;
    private static final double OVERLAP_PERCENT = 0.20;

    private CodeChunker() {}

    public static List<String> chunk(String content) {
        if (content == null || content.isBlank()) {
            return List.of("");
        }

        if (content.length() <= MAX_CHARS) {
            return List.of(content);
        }

        List<String> chunks = new ArrayList<>();
        String[] lines = content.split("\n");

        int overlapChars = (int) (MAX_CHARS * OVERLAP_PERCENT);
        int chunkStart = 0;
        int chunkNum = 0;

        while (chunkStart < lines.length) {
            StringBuilder chunkContent = new StringBuilder();
            int chunkLineStart = chunkStart;
            int chunkLineEnd = chunkStart;

            // Fill chunk up to MAX_CHARS
            while (chunkLineEnd < lines.length) {
                String nextLine = lines[chunkLineEnd] + "\n";
                if (chunkContent.length() + nextLine.length() > MAX_CHARS
                        && chunkContent.length() > 0) {
                    break;
                }
                chunkContent.append(nextLine);
                chunkLineEnd++;
            }

            // Prevent infinite loop — advance at least 1 line
            if (chunkLineEnd == chunkStart) {
                chunkContent.append(lines[chunkLineEnd]).append("\n");
                chunkLineEnd++;
            }

            chunks.add(chunkContent.toString().stripTrailing());
            chunkNum++;

            // Next chunk starts with overlap — go back overlapChars worth of lines
            int overlapLineCount = 0;
            int tempChars = 0;
            for (int i = chunkLineEnd - 1; i >= chunkLineStart; i--) {
                tempChars += lines[i].length() + 1;
                if (tempChars >= overlapChars) {
                    overlapLineCount = chunkLineEnd - i;
                    break;
                }
            }

            chunkStart = chunkLineEnd - Math.min(overlapLineCount, chunkLineEnd - chunkLineStart - 1);

            // Safety — if we didn't advance, force forward
            if (chunkStart >= chunkLineEnd) {
                chunkStart = chunkLineEnd;
            }
        }

        // Add chunk headers now that we know total count
        int total = chunks.size();
        List<String> headeredChunks = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            String header = String.format("// [Chunk %d/%d]\n", i + 1, total);
            headeredChunks.add(header + chunks.get(i));
        }

        log.debug("Chunked content into {} chunks (original size: {} chars)",
                total, content.length());

        return headeredChunks;
    }

    public static boolean needsChunking(String content) {
        return content != null && content.length() > MAX_CHARS;
    }
}