package com.aicodereview.fetch.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeChunkerTest {

    @Test
    void chunk_smallFile_returnsSingleChunk() {
        String content = "public class Small { }";

        List<String> chunks = CodeChunker.chunk(content);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).contains("Small");
    }

    @Test
    void chunk_largeFile_returnsMultipleChunks() {
        // Generate content larger than MAX_CHARS (12000)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("public void method").append(i)
              .append("() { System.out.println(\"line ").append(i).append("\"); }\n");
        }
        String largeContent = sb.toString();

        assertThat(largeContent.length()).isGreaterThan(CodeChunker.MAX_CHARS);

        List<String> chunks = CodeChunker.chunk(largeContent);

        assertThat(chunks.size()).isGreaterThan(1);
    }

    @Test
    void chunk_largeFile_noContentLost() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("uniqueMethod").append(i).append("\n");
        }
        String content = sb.toString();

        List<String> chunks = CodeChunker.chunk(content);
        String combined = String.join("", chunks);

        // Every unique method name should appear somewhere in chunks
        assertThat(combined).contains("uniqueMethod0");
        assertThat(combined).contains("uniqueMethod499");
    }

    @Test
    void chunk_eachChunk_withinSizeLimit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("line ").append(i).append(" some content here\n");
        }

        List<String> chunks = CodeChunker.chunk(sb.toString());

        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(CodeChunker.MAX_CHARS + 500);
        }
    }

    @Test
    void needsChunking_smallContent_returnsFalse() {
        assertThat(CodeChunker.needsChunking("short content")).isFalse();
    }

    @Test
    void needsChunking_largeContent_returnsTrue() {
        String large = "x".repeat(CodeChunker.MAX_CHARS + 1);
        assertThat(CodeChunker.needsChunking(large)).isTrue();
    }

    @Test
    void needsChunking_null_returnsFalse() {
        assertThat(CodeChunker.needsChunking(null)).isFalse();
    }
}