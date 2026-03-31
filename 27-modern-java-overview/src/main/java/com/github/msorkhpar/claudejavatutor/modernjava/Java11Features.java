package com.github.msorkhpar.claudejavatutor.modernjava;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Demonstrates key features introduced in Java 11 (LTS).
 * Covers: Local-Variable Syntax for Lambda Parameters, HTTP Client API,
 * String API Enhancements, Nested Based Access Control, Running Java Files Directly.
 */
public class Java11Features {

    // ========== Local-Variable Syntax for Lambda Parameters (var in lambdas) ==========

    /**
     * Demonstrates using var in lambda parameters (allows annotations on lambda params).
     * In Java 11, you can use var for lambda formal parameters.
     */
    public static List<String> transformWithVar(List<String> input) {
        // Using var allows adding annotations to lambda parameters
        Function<String, String> transformer = (var s) -> s.toUpperCase();
        return input.stream().map(transformer).collect(Collectors.toList());
    }

    /**
     * Demonstrates var in multi-parameter lambda.
     */
    public static List<String> sortWithVar(List<String> input) {
        List<String> result = new java.util.ArrayList<>(input);
        result.sort((var a, var b) -> a.compareToIgnoreCase(b));
        return result;
    }

    // ========== HTTP Client API ==========

    /**
     * Creates an HttpClient with custom configuration.
     * The HTTP Client API was standardized in Java 11 (incubated in Java 9).
     */
    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Builds an HTTP GET request.
     */
    public static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    /**
     * Builds an HTTP POST request with body.
     */
    public static HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    /**
     * Sends a synchronous HTTP request and returns the status code.
     */
    public static int sendRequestGetStatus(HttpClient client, HttpRequest request)
            throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    // ========== String API Enhancements ==========

    /**
     * Demonstrates String.isBlank() - checks if string is empty or contains only whitespace.
     */
    public static boolean isBlankString(String input) {
        return input.isBlank();
    }

    /**
     * Demonstrates String.strip() - removes leading and trailing whitespace (Unicode-aware).
     */
    public static String stripString(String input) {
        return input.strip();
    }

    /**
     * Demonstrates String.stripLeading() and String.stripTrailing().
     */
    public static String stripLeading(String input) {
        return input.stripLeading();
    }

    public static String stripTrailing(String input) {
        return input.stripTrailing();
    }

    /**
     * Demonstrates String.lines() - splits string into stream of lines.
     */
    public static List<String> splitIntoLines(String input) {
        return input.lines().collect(Collectors.toList());
    }

    /**
     * Demonstrates String.repeat() - repeats string n times.
     */
    public static String repeatString(String input, int count) {
        return input.repeat(count);
    }

    /**
     * Demonstrates difference between strip() and trim().
     * strip() is Unicode-aware, trim() only handles ASCII whitespace (<= U+0020).
     */
    public static boolean stripVsTrimDifference(String input) {
        String stripped = input.strip();
        String trimmed = input.trim();
        return !stripped.equals(trimmed);
    }

    // ========== Nested Based Access Control ==========

    /**
     * Demonstrates nested classes accessing private members of enclosing class.
     * In Java 11, the JVM recognizes nest-based access control natively,
     * eliminating the need for synthetic bridge methods.
     */
    public static class Outer {
        private String secret = "outer-secret";
        private int value = 42;

        public String getSecret() {
            return secret;
        }

        /**
         * Inner class accessing private members of Outer - no bridge methods needed in Java 11+.
         */
        public class Inner {
            private String innerSecret = "inner-secret";

            public String accessOuterSecret() {
                return secret; // Direct access to outer's private field
            }

            public int accessOuterValue() {
                return value; // Direct access to outer's private field
            }
        }

        /**
         * Outer accessing inner's private member - also supported natively in Java 11+.
         */
        public String accessInnerSecret() {
            Inner inner = new Inner();
            return inner.innerSecret; // Direct access to inner's private field
        }

        /**
         * Returns the nest host class.
         */
        public Class<?> getNestHost() {
            return Outer.class.getNestHost();
        }

        /**
         * Returns nest members.
         */
        public Class<?>[] getNestMembers() {
            return Outer.class.getNestMembers();
        }

        /**
         * Checks if two classes are nestmates.
         */
        public boolean isNestmate() {
            return Outer.class.isNestmateOf(Inner.class);
        }
    }

    // ========== Running Java Files Directly ==========

    /**
     * Demonstrates the concept of single-file source-code programs.
     * In Java 11, you can run: java MyProgram.java (without explicit compilation).
     * This method simulates the behavior by writing and reading a temp file.
     */
    public static String simulateSingleFileExecution(String javaCode) throws IOException {
        Path tempFile = Files.createTempFile("JavaProgram", ".java");
        try {
            Files.writeString(tempFile, javaCode);
            String content = Files.readString(tempFile);
            return content;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Demonstrates Files.writeString() and Files.readString() added in Java 11.
     */
    public static String writeAndReadFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("java11-", ".txt");
        try {
            Files.writeString(tempFile, content);
            return Files.readString(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Demonstrates Optional enhancements in Java 11: isEmpty().
     */
    public static boolean isOptionalEmpty(Optional<String> opt) {
        return opt.isEmpty(); // New in Java 11
    }
}
