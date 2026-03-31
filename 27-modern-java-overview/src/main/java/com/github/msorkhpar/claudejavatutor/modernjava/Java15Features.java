package com.github.msorkhpar.claudejavatutor.modernjava;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Demonstrates key features introduced in Java 15.
 * Covers: Text Blocks (Standard), Pattern Matching for instanceof (Second Preview),
 * Hidden Classes.
 */
public class Java15Features {

    // ========== Text Blocks ==========

    /**
     * Demonstrates basic text block usage.
     * Text blocks use triple-quote delimiters and preserve formatting.
     */
    public static String basicTextBlock() {
        return """
                Hello,
                World!
                """;
    }

    /**
     * Demonstrates text block for JSON.
     */
    public static String jsonTextBlock(String name, int age) {
        return """
                {
                    "name": "%s",
                    "age": %d
                }
                """.formatted(name, age);
    }

    /**
     * Demonstrates text block for SQL.
     */
    public static String sqlTextBlock(String tableName) {
        return """
                SELECT id, name, email
                FROM %s
                WHERE active = true
                ORDER BY name ASC
                """.formatted(tableName);
    }

    /**
     * Demonstrates text block for HTML.
     */
    public static String htmlTextBlock(String title, String body) {
        return """
                <html>
                    <head>
                        <title>%s</title>
                    </head>
                    <body>
                        <p>%s</p>
                    </body>
                </html>
                """.formatted(title, body);
    }

    /**
     * Demonstrates escape sequences in text blocks.
     * \s prevents trailing whitespace stripping.
     * \ at end of line prevents line terminator insertion.
     */
    public static String textBlockWithEscapes() {
        return """
                line1\s\s
                line2 \
                continues here
                """;
    }

    /**
     * Demonstrates text block indentation control.
     * The closing delimiter position controls the common leading whitespace removal.
     */
    public static String textBlockIndentation() {
        // No indentation - closing quotes at left margin
        return """
no indent
  two spaces
    four spaces
""";
    }

    /**
     * Demonstrates that text block trailing newline can be controlled.
     */
    public static String textBlockNoTrailingNewline() {
        return """
                content without trailing newline\
                """;
    }

    // ========== Pattern Matching for instanceof ==========

    /**
     * Demonstrates pattern matching for instanceof (standard in Java 16, preview in 15).
     * Eliminates the need for explicit casting after instanceof check.
     */
    public static String describeObject(Object obj) {
        if (obj instanceof String s) {
            return "String of length " + s.length();
        } else if (obj instanceof Integer i) {
            return "Integer with value " + i;
        } else if (obj instanceof Double d) {
            return "Double with value " + String.format("%.2f", d);
        } else if (obj instanceof List<?> list) {
            return "List with " + list.size() + " elements";
        } else if (obj == null) {
            return "null value";
        } else {
            return "Unknown type: " + obj.getClass().getSimpleName();
        }
    }

    /**
     * Demonstrates pattern matching with combined conditions.
     */
    public static boolean isLongString(Object obj) {
        return obj instanceof String s && s.length() > 10;
    }

    /**
     * Demonstrates pattern matching in a real-world scenario: processing different shapes.
     */
    public sealed interface Shape permits Circle, Rectangle, Triangle {
    }

    public record Circle(double radius) implements Shape {
    }

    public record Rectangle(double width, double height) implements Shape {
    }

    public record Triangle(double base, double height) implements Shape {
    }

    public static double calculateArea(Shape shape) {
        if (shape instanceof Circle c) {
            return Math.PI * c.radius() * c.radius();
        } else if (shape instanceof Rectangle r) {
            return r.width() * r.height();
        } else if (shape instanceof Triangle t) {
            return 0.5 * t.base() * t.height();
        }
        throw new IllegalArgumentException("Unknown shape: " + shape);
    }

    /**
     * Demonstrates pattern matching with negation.
     */
    public static boolean isNotEmptyString(Object obj) {
        return obj instanceof String s && !s.isEmpty();
    }

    // ========== Hidden Classes ==========

    /**
     * Hidden classes are classes that cannot be discovered or used by name.
     * They are intended for frameworks that generate classes at runtime.
     * This method demonstrates the concept (actual hidden class creation
     * requires the Lookup.defineHiddenClass API).
     */
    public static String explainHiddenClasses() {
        return """
                Hidden Classes (JEP 371):
                - Cannot be discovered by other classes
                - Cannot be used by name in bytecode
                - Ideal for frameworks generating classes at runtime
                - Unloaded independently of the class that created them
                - Created via MethodHandles.Lookup.defineHiddenClass()
                """;
    }

    /**
     * Demonstrates a proxy-like pattern that hidden classes are used for.
     * Frameworks like Spring use hidden classes for dynamic proxies.
     */
    @FunctionalInterface
    public interface DynamicAction {
        String execute(String input);
    }

    public static DynamicAction createDynamicAction(String prefix) {
        // In real frameworks, this would use hidden classes for the lambda implementation
        return input -> prefix + ": " + input;
    }

    /**
     * Shows how to check if a class is hidden.
     */
    public static boolean isHiddenClass(Class<?> clazz) {
        return clazz.isHidden();
    }

    /**
     * Demonstrates the use of lambda expressions which internally use hidden classes
     * (since Java 15, the JVM uses hidden classes for lambda proxy classes).
     */
    public static List<String> processWithLambda(List<String> input) {
        return input.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
