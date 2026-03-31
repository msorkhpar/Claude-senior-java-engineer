package com.github.msorkhpar.claudejavatutor.modernjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Java 11 Features Tests")
class Java11FeaturesTest {

    @Nested
    @DisplayName("Local-Variable Syntax for Lambda Parameters")
    class VarInLambdaTests {

        @Test
        @DisplayName("Should transform strings using var in lambda")
        void testTransformWithVar() {
            List<String> input = Arrays.asList("hello", "world");
            List<String> result = Java11Features.transformWithVar(input);
            assertThat(result).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("Should handle empty list with var lambda")
        void testTransformEmptyList() {
            List<String> result = Java11Features.transformWithVar(Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should sort with var in multi-parameter lambda")
        void testSortWithVar() {
            List<String> input = Arrays.asList("Charlie", "alice", "Bob");
            List<String> result = Java11Features.sortWithVar(input);
            assertThat(result).containsExactly("alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should not modify original list")
        void testSortDoesNotModifyOriginal() {
            List<String> original = new ArrayList<>(Arrays.asList("C", "A", "B"));
            Java11Features.sortWithVar(original);
            assertThat(original).containsExactly("C", "A", "B");
        }
    }

    @Nested
    @DisplayName("HTTP Client API")
    class HttpClientTests {

        @Test
        @DisplayName("Should create HTTP client with HTTP/2")
        void testCreateHttpClient() {
            HttpClient client = Java11Features.createHttpClient();
            assertThat(client).isNotNull();
            assertThat(client.version()).isEqualTo(HttpClient.Version.HTTP_2);
            assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NORMAL);
        }

        @Test
        @DisplayName("Should build GET request with correct URI")
        void testBuildGetRequest() {
            HttpRequest request = Java11Features.buildGetRequest("https://example.com/api");
            assertThat(request.uri().toString()).isEqualTo("https://example.com/api");
            assertThat(request.method()).isEqualTo("GET");
        }

        @Test
        @DisplayName("Should build POST request with body")
        void testBuildPostRequest() {
            HttpRequest request = Java11Features.buildPostRequest(
                    "https://example.com/api", "{\"key\":\"value\"}");
            assertThat(request.uri().toString()).isEqualTo("https://example.com/api");
            assertThat(request.method()).isEqualTo("POST");
        }

        @Test
        @DisplayName("Should build request with correct headers")
        void testRequestHeaders() {
            HttpRequest request = Java11Features.buildGetRequest("https://example.com");
            assertThat(request.headers().firstValue("Accept")).hasValue("application/json");
        }
    }

    @Nested
    @DisplayName("String API Enhancements")
    class StringApiTests {

        @Test
        @DisplayName("Should detect blank strings")
        void testIsBlank() {
            assertThat(Java11Features.isBlankString("")).isTrue();
            assertThat(Java11Features.isBlankString("   ")).isTrue();
            assertThat(Java11Features.isBlankString("\t\n ")).isTrue();
            assertThat(Java11Features.isBlankString("hello")).isFalse();
            assertThat(Java11Features.isBlankString(" a ")).isFalse();
        }

        @Test
        @DisplayName("Should strip whitespace from both ends")
        void testStrip() {
            assertThat(Java11Features.stripString("  hello  ")).isEqualTo("hello");
            assertThat(Java11Features.stripString("\t hello \n")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should strip leading whitespace only")
        void testStripLeading() {
            assertThat(Java11Features.stripLeading("  hello  ")).isEqualTo("hello  ");
        }

        @Test
        @DisplayName("Should strip trailing whitespace only")
        void testStripTrailing() {
            assertThat(Java11Features.stripTrailing("  hello  ")).isEqualTo("  hello");
        }

        @Test
        @DisplayName("Should split string into lines")
        void testSplitIntoLines() {
            List<String> lines = Java11Features.splitIntoLines("line1\nline2\nline3");
            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("Should split empty string into empty list")
        void testSplitEmptyString() {
            List<String> lines = Java11Features.splitIntoLines("");
            assertThat(lines).isEmpty();
        }

        @Test
        @DisplayName("Should handle different line separators")
        void testSplitDifferentSeparators() {
            List<String> lines = Java11Features.splitIntoLines("a\nb\r\nc\rd");
            assertThat(lines).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Should repeat string n times")
        void testRepeatString() {
            assertThat(Java11Features.repeatString("ab", 3)).isEqualTo("ababab");
            assertThat(Java11Features.repeatString("x", 0)).isEmpty();
            assertThat(Java11Features.repeatString("", 5)).isEmpty();
        }

        @Test
        @DisplayName("Should detect strip vs trim difference for Unicode whitespace")
        void testStripVsTrimDifference() {
            // Unicode character \u2000 (EN QUAD) is a Unicode whitespace
            // strip() handles it, trim() does not
            String unicodeWhitespace = "\u2000hello\u2000";
            assertThat(Java11Features.stripVsTrimDifference(unicodeWhitespace)).isTrue();
        }

        @Test
        @DisplayName("Should show no difference for ASCII whitespace")
        void testStripVsTrimNoDifference() {
            String asciiWhitespace = " hello ";
            assertThat(Java11Features.stripVsTrimDifference(asciiWhitespace)).isFalse();
        }
    }

    @Nested
    @DisplayName("Nested Based Access Control")
    class NestedAccessTests {

        @Test
        @DisplayName("Inner class should access outer private field")
        void testInnerAccessesOuterPrivate() {
            Java11Features.Outer outer = new Java11Features.Outer();
            Java11Features.Outer.Inner inner = outer.new Inner();
            assertThat(inner.accessOuterSecret()).isEqualTo("outer-secret");
        }

        @Test
        @DisplayName("Inner class should access outer private value")
        void testInnerAccessesOuterValue() {
            Java11Features.Outer outer = new Java11Features.Outer();
            Java11Features.Outer.Inner inner = outer.new Inner();
            assertThat(inner.accessOuterValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Outer should access inner private field")
        void testOuterAccessesInnerPrivate() {
            Java11Features.Outer outer = new Java11Features.Outer();
            assertThat(outer.accessInnerSecret()).isEqualTo("inner-secret");
        }

        @Test
        @DisplayName("Should return correct nest host")
        void testNestHost() {
            Java11Features.Outer outer = new Java11Features.Outer();
            assertThat(outer.getNestHost()).isEqualTo(Java11Features.class);
        }

        @Test
        @DisplayName("Should include inner class in nest members")
        void testNestMembers() {
            Java11Features.Outer outer = new Java11Features.Outer();
            Class<?>[] members = outer.getNestMembers();
            assertThat(members).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should confirm Outer and Inner are nestmates")
        void testIsNestmate() {
            Java11Features.Outer outer = new Java11Features.Outer();
            assertThat(outer.isNestmate()).isTrue();
        }
    }

    @Nested
    @DisplayName("Running Java Files Directly / File API Enhancements")
    class FileApiTests {

        @Test
        @DisplayName("Should write and read file content")
        void testWriteAndRead() throws IOException {
            String content = "Hello, Java 11!";
            String result = Java11Features.writeAndReadFile(content);
            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("Should handle empty content")
        void testWriteAndReadEmpty() throws IOException {
            String result = Java11Features.writeAndReadFile("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiline content")
        void testWriteAndReadMultiline() throws IOException {
            String content = "line1\nline2\nline3";
            String result = Java11Features.writeAndReadFile(content);
            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("Should simulate single-file execution")
        void testSimulateSingleFile() throws IOException {
            String javaCode = "public class Hello { public static void main(String[] args) {} }";
            String result = Java11Features.simulateSingleFileExecution(javaCode);
            assertThat(result).isEqualTo(javaCode);
        }
    }

    @Nested
    @DisplayName("Optional Enhancements")
    class OptionalTests {

        @Test
        @DisplayName("Should return true for empty optional")
        void testIsOptionalEmpty() {
            assertThat(Java11Features.isOptionalEmpty(Optional.empty())).isTrue();
        }

        @Test
        @DisplayName("Should return false for present optional")
        void testIsOptionalNotEmpty() {
            assertThat(Java11Features.isOptionalEmpty(Optional.of("value"))).isFalse();
        }
    }
}
