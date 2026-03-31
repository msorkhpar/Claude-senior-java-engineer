package com.github.msorkhpar.claudejavatutor.textblocks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates practical use cases and benefits of Java text blocks.
 * Covers JSON, SQL, HTML, XML, code generation, and other real-world scenarios.
 */
public class TextBlockUseCases {

    /**
     * Generates a JSON response body using a text block.
     */
    public static String createJsonResponse(int statusCode, String message, String data) {
        return """
                {
                    "status": %d,
                    "message": "%s",
                    "data": %s
                }""".formatted(statusCode, message, data);
    }

    /**
     * Generates a parameterized SQL query using a text block.
     */
    public static String createSelectQuery(String table, List<String> columns, String whereClause) {
        String cols = columns.isEmpty() ? "*" : String.join(", ", columns);
        return """
                SELECT %s
                FROM %s
                WHERE %s""".formatted(cols, table, whereClause);
    }

    /**
     * Generates an HTML email template using a text block.
     */
    public static String createEmailTemplate(String recipientName, String subject, String bodyContent) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body>
                    <h1>Dear %s,</h1>
                    <div>
                        %s
                    </div>
                    <footer>
                        <p>Best regards,<br>The Team</p>
                    </footer>
                </body>
                </html>""".formatted(subject, recipientName, bodyContent);
    }

    /**
     * Generates a Java source file using text blocks (code generation use case).
     */
    public static String generateJavaClass(String packageName, String className, Map<String, String> fields) {
        String fieldDeclarations = fields.entrySet().stream()
                .map(e -> "    private %s %s;".formatted(e.getValue(), e.getKey()))
                .collect(Collectors.joining("\n"));

        return """
                package %s;

                public class %s {

                %s

                    public %s() {
                    }
                }""".formatted(packageName, className, fieldDeclarations, className);
    }

    /**
     * Creates a YAML configuration snippet using text blocks.
     */
    public static String createYamlConfig(String appName, int port, String profile) {
        return """
                application:
                  name: %s
                  server:
                    port: %d
                  spring:
                    profiles:
                      active: %s""".formatted(appName, port, profile);
    }

    /**
     * Creates a multi-line log message for structured logging.
     */
    public static String createStructuredLogMessage(String level, String source, String message, String traceId) {
        return """
                [%s] source=%s traceId=%s
                message=%s""".formatted(level, source, traceId, message);
    }

    /**
     * Demonstrates using text blocks for regex patterns with documentation.
     */
    public static String documentedRegexPattern() {
        // The pattern itself still needs standard escaping,
        // but the surrounding documentation benefits from text block readability.
        return """
                Pattern: Email Validation
                Regex: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$
                Description: Matches standard email addresses
                Examples: user@example.com, admin@sub.domain.org""";
    }

    /**
     * Demonstrates text blocks for test data fixtures.
     */
    public static String createTestFixtureCsv() {
        return """
                id,name,email,active
                1,Alice,alice@example.com,true
                2,Bob,bob@example.com,true
                3,Charlie,charlie@example.com,false""";
    }

    /**
     * Demonstrates text blocks for shell script generation.
     */
    public static String createBashScript(String directory, String logFile) {
        return """
                #!/bin/bash
                set -euo pipefail

                WORK_DIR="%s"
                LOG_FILE="%s"

                echo "Starting process..." >> "$LOG_FILE"
                cd "$WORK_DIR"
                echo "Done." >> "$LOG_FILE"
                """.formatted(directory, logFile);
    }

    /**
     * Demonstrates how text blocks improve readability of error messages.
     */
    public static String createDetailedErrorMessage(String operation, String entity, String reason) {
        return """
                Operation failed: %s
                Entity: %s
                Reason: %s
                Please check the input parameters and try again.""".formatted(operation, entity, reason);
    }

    /**
     * Demonstrates using text blocks for multiline string comparison in tests.
     */
    public static String expectedOutput() {
        return """
                Name: Alice
                Age: 30
                Role: Engineer
                Status: Active""";
    }

    /**
     * Demonstrates benefits of text blocks for Markdown content generation.
     */
    public static String createMarkdownReport(String title, List<String> items) {
        String bulletPoints = items.stream()
                .map(item -> "- " + item)
                .collect(Collectors.joining("\n"));
        return """
                # %s

                ## Summary

                %s""".formatted(title, bulletPoints);
    }
}
