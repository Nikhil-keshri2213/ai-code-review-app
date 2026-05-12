package com.aicodereview.fetch.util;

import com.aicodereview.fetch.dto.DiffResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiffParserTest {

    @Test
    void parse_singleHunk_returnsCorrectCounts() {
        String patch = """
                @@ -1,4 +1,6 @@
                 public class Hello {
                +    private String name;
                +    private int age;
                     public void greet() {
                -        System.out.println("hi");
                +        System.out.println("hello " + name);
                     }
                 }
                """;

        DiffResult result = DiffParser.parse(patch, "Hello.java");

        assertThat(result.isParsedSuccessfully()).isTrue();
        assertThat(result.getFileName()).isEqualTo("Hello.java");
        assertThat(result.getTotalAdditions()).isEqualTo(3);
        assertThat(result.getTotalDeletions()).isEqualTo(1);
        assertThat(result.getHunks()).hasSize(1);
    }

    @Test
    void parse_multiHunk_returnsMultipleHunks() {
        String patch = """
                @@ -1,3 +1,4 @@
                 line1
                +added1
                 line2
                 line3
                @@ -10,3 +11,4 @@
                 line10
                +added2
                 line11
                 line12
                """;

        DiffResult result = DiffParser.parse(patch, "Multi.java");

        assertThat(result.isParsedSuccessfully()).isTrue();
        assertThat(result.getHunks()).hasSize(2);
        assertThat(result.getTotalAdditions()).isEqualTo(2);
    }

    @Test
    void parse_nullPatch_returnsFailed() {
        DiffResult result = DiffParser.parse(null, "Empty.java");

        assertThat(result.isParsedSuccessfully()).isFalse();
        assertThat(result.getHunks()).isEmpty();
    }

    @Test
    void parse_emptyPatch_returnsFailed() {
        DiffResult result = DiffParser.parse("", "Empty.java");

        assertThat(result.isParsedSuccessfully()).isFalse();
        assertThat(result.getHunks()).isEmpty();
    }

    @Test
    void extractChangedCode_withAdditions_returnsFormattedOutput() {
        String patch = """
                @@ -1,3 +1,4 @@
                 public class Foo {
                +    private String bar;
                     public void run() {}
                 }
                """;

        DiffResult result = DiffParser.parse(patch, "Foo.java");
        String extracted = DiffParser.extractChangedCode(result);

        assertThat(extracted).isNotBlank();
        assertThat(extracted).contains("bar");
    }

    @Test
    void extractChangedCode_emptyResult_returnsEmpty() {
        DiffResult empty = DiffResult.builder()
                .fileName("test.java")
                .hunks(java.util.List.of())
                .parsedSuccessfully(false)
                .build();

        String result = DiffParser.extractChangedCode(empty);
        assertThat(result).isEmpty();
    }
}