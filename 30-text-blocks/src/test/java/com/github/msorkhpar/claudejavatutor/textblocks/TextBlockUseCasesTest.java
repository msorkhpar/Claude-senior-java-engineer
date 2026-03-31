package com.github.msorkhpar.claudejavatutor.textblocks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Text Block Use Cases Tests")
class TextBlockUseCasesTest {

    @Nested
    @DisplayName("JSON Response")
    class JsonResponseTest {

        @Test
        @DisplayName("Should create JSON with status code and message")
        void testCreateJsonResponse() {
            String result = TextBlockUseCases.createJsonResponse(200, "OK", "null");

            assertThat(result)
                    .contains("\"status\": 200")
                    .contains("\"message\": \"OK\"")
                    .contains("\"data\": null");
        }

        @Test
        @DisplayName("Should handle error response")
        void testCreateJsonErrorResponse() {
            String result = TextBlockUseCases.createJsonResponse(500, "Internal Server Error", "null");

            assertThat(result)
                    .contains("\"status\": 500")
                    .contains("\"message\": \"Internal Server Error\"");
        }

        @Test
        @DisplayName("Should handle nested JSON data")
        void testCreateJsonWithNestedData() {
            String data = "{\"id\": 1, \"name\": \"test\"}";
            String result = TextBlockUseCases.createJsonResponse(200, "Success", data);

            assertThat(result).contains("\"data\": {\"id\": 1, \"name\": \"test\"}");
        }
    }

    @Nested
    @DisplayName("SQL Query")
    class SqlQueryTest {

        @Test
        @DisplayName("Should create SELECT query with specific columns")
        void testCreateSelectQueryWithColumns() {
            String result = TextBlockUseCases.createSelectQuery("users",
                    List.of("name", "email"), "active = true");

            assertThat(result)
                    .contains("SELECT name, email")
                    .contains("FROM users")
                    .contains("WHERE active = true");
        }

        @Test
        @DisplayName("Should use * when columns list is empty")
        void testCreateSelectQueryWithEmptyColumns() {
            String result = TextBlockUseCases.createSelectQuery("orders",
                    Collections.emptyList(), "id > 0");

            assertThat(result).contains("SELECT *");
        }

        @Test
        @DisplayName("Should handle single column")
        void testCreateSelectQueryWithSingleColumn() {
            String result = TextBlockUseCases.createSelectQuery("products",
                    List.of("name"), "price > 10");

            assertThat(result).contains("SELECT name");
        }
    }

    @Nested
    @DisplayName("HTML Email Template")
    class HtmlEmailTemplateTest {

        @Test
        @DisplayName("Should create HTML email with recipient and content")
        void testCreateEmailTemplate() {
            String result = TextBlockUseCases.createEmailTemplate("Alice", "Welcome", "Your account is ready.");

            assertThat(result)
                    .contains("<title>Welcome</title>")
                    .contains("Dear Alice,")
                    .contains("Your account is ready.")
                    .contains("<!DOCTYPE html>")
                    .contains("</html>");
        }

        @Test
        @DisplayName("Should include proper HTML structure")
        void testEmailTemplateStructure() {
            String result = TextBlockUseCases.createEmailTemplate("Bob", "Info", "content");

            assertThat(result)
                    .contains("<html>")
                    .contains("<head>")
                    .contains("<body>")
                    .contains("<footer>")
                    .contains("Best regards");
        }
    }

    @Nested
    @DisplayName("Java Class Generation")
    class JavaClassGenerationTest {

        @Test
        @DisplayName("Should generate class with fields")
        void testGenerateJavaClass() {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("name", "String");
            fields.put("age", "int");

            String result = TextBlockUseCases.generateJavaClass("com.example", "User", fields);

            assertThat(result)
                    .contains("package com.example;")
                    .contains("public class User")
                    .contains("private String name;")
                    .contains("private int age;")
                    .contains("public User()");
        }

        @Test
        @DisplayName("Should generate class with no fields")
        void testGenerateJavaClassNoFields() {
            String result = TextBlockUseCases.generateJavaClass("com.example", "Empty",
                    Collections.emptyMap());

            assertThat(result)
                    .contains("public class Empty")
                    .contains("public Empty()");
        }
    }

    @Nested
    @DisplayName("YAML Configuration")
    class YamlConfigTest {

        @Test
        @DisplayName("Should create YAML config with app settings")
        void testCreateYamlConfig() {
            String result = TextBlockUseCases.createYamlConfig("my-service", 8080, "production");

            assertThat(result)
                    .contains("name: my-service")
                    .contains("port: 8080")
                    .contains("active: production");
        }

        @Test
        @DisplayName("Should handle different profiles")
        void testCreateYamlConfigDevProfile() {
            String result = TextBlockUseCases.createYamlConfig("dev-app", 9090, "dev");

            assertThat(result)
                    .contains("name: dev-app")
                    .contains("port: 9090")
                    .contains("active: dev");
        }
    }

    @Nested
    @DisplayName("Structured Log Message")
    class StructuredLogMessageTest {

        @Test
        @DisplayName("Should create structured log message")
        void testCreateStructuredLogMessage() {
            String result = TextBlockUseCases.createStructuredLogMessage(
                    "ERROR", "UserService", "User not found", "trace-123");

            assertThat(result)
                    .contains("[ERROR]")
                    .contains("source=UserService")
                    .contains("traceId=trace-123")
                    .contains("message=User not found");
        }
    }

    @Nested
    @DisplayName("Test Fixture CSV")
    class TestFixtureCsvTest {

        @Test
        @DisplayName("Should create CSV with header and data rows")
        void testCreateTestFixtureCsv() {
            String result = TextBlockUseCases.createTestFixtureCsv();

            assertThat(result.lines().count()).isEqualTo(4);
            assertThat(result).startsWith("id,name,email,active");
            assertThat(result).contains("alice@example.com");
        }

        @Test
        @DisplayName("CSV header should have correct columns")
        void testCsvHeader() {
            String result = TextBlockUseCases.createTestFixtureCsv();
            String header = result.lines().findFirst().orElse("");

            assertThat(header.split(",")).containsExactly("id", "name", "email", "active");
        }
    }

    @Nested
    @DisplayName("Bash Script")
    class BashScriptTest {

        @Test
        @DisplayName("Should create bash script with correct content")
        void testCreateBashScript() {
            String result = TextBlockUseCases.createBashScript("/var/data", "/var/log/app.log");

            assertThat(result)
                    .contains("#!/bin/bash")
                    .contains("set -euo pipefail")
                    .contains("WORK_DIR=\"/var/data\"")
                    .contains("LOG_FILE=\"/var/log/app.log\"");
        }

        @Test
        @DisplayName("Should end with trailing newline for valid bash script")
        void testBashScriptTrailingNewline() {
            String result = TextBlockUseCases.createBashScript("/tmp", "/tmp/out.log");

            assertThat(result).endsWith("\n");
        }
    }

    @Nested
    @DisplayName("Error Message")
    class ErrorMessageTest {

        @Test
        @DisplayName("Should create detailed error message")
        void testCreateDetailedErrorMessage() {
            String result = TextBlockUseCases.createDetailedErrorMessage(
                    "DELETE", "User#123", "Foreign key constraint violation");

            assertThat(result)
                    .contains("Operation failed: DELETE")
                    .contains("Entity: User#123")
                    .contains("Reason: Foreign key constraint violation")
                    .contains("Please check the input parameters");
        }
    }

    @Nested
    @DisplayName("Expected Output")
    class ExpectedOutputTest {

        @Test
        @DisplayName("Should create expected output for test assertions")
        void testExpectedOutput() {
            String result = TextBlockUseCases.expectedOutput();

            assertThat(result)
                    .contains("Name: Alice")
                    .contains("Age: 30")
                    .contains("Role: Engineer")
                    .contains("Status: Active");
        }

        @Test
        @DisplayName("Expected output should have four lines")
        void testExpectedOutputLineCount() {
            String result = TextBlockUseCases.expectedOutput();

            assertThat(result.lines().count()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Markdown Report")
    class MarkdownReportTest {

        @Test
        @DisplayName("Should create markdown with title and bullet points")
        void testCreateMarkdownReport() {
            String result = TextBlockUseCases.createMarkdownReport("Sprint Report",
                    List.of("Feature A completed", "Bug B fixed", "Refactored module C"));

            assertThat(result)
                    .contains("# Sprint Report")
                    .contains("## Summary")
                    .contains("- Feature A completed")
                    .contains("- Bug B fixed")
                    .contains("- Refactored module C");
        }

        @Test
        @DisplayName("Should handle empty items list")
        void testCreateMarkdownReportEmpty() {
            String result = TextBlockUseCases.createMarkdownReport("Empty Report",
                    Collections.emptyList());

            assertThat(result)
                    .contains("# Empty Report")
                    .contains("## Summary");
        }

        @Test
        @DisplayName("Should handle single item")
        void testCreateMarkdownReportSingleItem() {
            String result = TextBlockUseCases.createMarkdownReport("Title",
                    List.of("Only item"));

            assertThat(result).contains("- Only item");
        }

        @Test
        @DisplayName("Documented regex pattern should contain all sections")
        void testDocumentedRegexPattern() {
            String result = TextBlockUseCases.documentedRegexPattern();

            assertThat(result)
                    .contains("Pattern:")
                    .contains("Regex:")
                    .contains("Description:")
                    .contains("Examples:");
        }
    }
}
