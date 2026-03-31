package com.github.msorkhpar.claudejavatutor.modernjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Java 15 Features Tests")
class Java15FeaturesTest {

    @Nested
    @DisplayName("Text Blocks")
    class TextBlockTests {

        @Test
        @DisplayName("Should create basic text block with preserved formatting")
        void testBasicTextBlock() {
            String result = Java15Features.basicTextBlock();
            assertThat(result).contains("Hello,");
            assertThat(result).contains("World!");
            assertThat(result.lines().count()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should create JSON text block with interpolation")
        void testJsonTextBlock() {
            String json = Java15Features.jsonTextBlock("Alice", 30);
            assertThat(json).contains("\"name\": \"Alice\"");
            assertThat(json).contains("\"age\": 30");
        }

        @Test
        @DisplayName("Should create SQL text block")
        void testSqlTextBlock() {
            String sql = Java15Features.sqlTextBlock("users");
            assertThat(sql).contains("SELECT id, name, email");
            assertThat(sql).contains("FROM users");
            assertThat(sql).contains("WHERE active = true");
        }

        @Test
        @DisplayName("Should create HTML text block with parameters")
        void testHtmlTextBlock() {
            String html = Java15Features.htmlTextBlock("Test Page", "Hello World");
            assertThat(html).contains("<title>Test Page</title>");
            assertThat(html).contains("<p>Hello World</p>");
            assertThat(html).contains("<html>");
            assertThat(html).contains("</html>");
        }

        @Test
        @DisplayName("Should handle escape sequences in text blocks")
        void testTextBlockWithEscapes() {
            String result = Java15Features.textBlockWithEscapes();
            // \s preserves trailing spaces
            assertThat(result).contains("line1");
            // line continuation with backslash
            assertThat(result).contains("continues here");
        }

        @Test
        @DisplayName("Should control indentation in text blocks")
        void testTextBlockIndentation() {
            String result = Java15Features.textBlockIndentation();
            assertThat(result).startsWith("no indent");
            assertThat(result).contains("  two spaces");
            assertThat(result).contains("    four spaces");
        }

        @Test
        @DisplayName("Should handle text block without trailing newline")
        void testTextBlockNoTrailingNewline() {
            String result = Java15Features.textBlockNoTrailingNewline();
            assertThat(result).doesNotEndWith("\n");
            assertThat(result).contains("content without trailing newline");
        }
    }

    @Nested
    @DisplayName("Pattern Matching for instanceof")
    class PatternMatchingTests {

        @Test
        @DisplayName("Should describe String objects")
        void testDescribeString() {
            assertThat(Java15Features.describeObject("hello"))
                    .isEqualTo("String of length 5");
        }

        @Test
        @DisplayName("Should describe Integer objects")
        void testDescribeInteger() {
            assertThat(Java15Features.describeObject(42))
                    .isEqualTo("Integer with value 42");
        }

        @Test
        @DisplayName("Should describe Double objects")
        void testDescribeDouble() {
            assertThat(Java15Features.describeObject(3.14))
                    .isEqualTo("Double with value 3.14");
        }

        @Test
        @DisplayName("Should describe List objects")
        void testDescribeList() {
            assertThat(Java15Features.describeObject(List.of(1, 2, 3)))
                    .isEqualTo("List with 3 elements");
        }

        @Test
        @DisplayName("Should describe null")
        void testDescribeNull() {
            assertThat(Java15Features.describeObject(null))
                    .isEqualTo("null value");
        }

        @Test
        @DisplayName("Should describe unknown types")
        void testDescribeUnknown() {
            assertThat(Java15Features.describeObject(new Object()))
                    .isEqualTo("Unknown type: Object");
        }

        @Test
        @DisplayName("Should detect long strings")
        void testIsLongString() {
            assertThat(Java15Features.isLongString("short")).isFalse();
            assertThat(Java15Features.isLongString("this is a very long string")).isTrue();
            assertThat(Java15Features.isLongString(42)).isFalse();
            assertThat(Java15Features.isLongString(null)).isFalse();
        }

        @Test
        @DisplayName("Should detect non-empty strings")
        void testIsNotEmptyString() {
            assertThat(Java15Features.isNotEmptyString("hello")).isTrue();
            assertThat(Java15Features.isNotEmptyString("")).isFalse();
            assertThat(Java15Features.isNotEmptyString(42)).isFalse();
            assertThat(Java15Features.isNotEmptyString(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Shape Area Calculations (Pattern Matching Use Case)")
    class ShapeTests {

        @Test
        @DisplayName("Should calculate circle area")
        void testCircleArea() {
            double area = Java15Features.calculateArea(new Java15Features.Circle(5.0));
            assertThat(area).isCloseTo(Math.PI * 25, within(0.001));
        }

        @Test
        @DisplayName("Should calculate rectangle area")
        void testRectangleArea() {
            double area = Java15Features.calculateArea(new Java15Features.Rectangle(4.0, 6.0));
            assertThat(area).isEqualTo(24.0);
        }

        @Test
        @DisplayName("Should calculate triangle area")
        void testTriangleArea() {
            double area = Java15Features.calculateArea(new Java15Features.Triangle(10.0, 5.0));
            assertThat(area).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Should handle zero-sized shapes")
        void testZeroSizedShapes() {
            assertThat(Java15Features.calculateArea(new Java15Features.Circle(0.0))).isZero();
            assertThat(Java15Features.calculateArea(new Java15Features.Rectangle(0.0, 5.0))).isZero();
            assertThat(Java15Features.calculateArea(new Java15Features.Triangle(0.0, 5.0))).isZero();
        }

        @Test
        @DisplayName("Should handle large shape dimensions")
        void testLargeShapes() {
            double area = Java15Features.calculateArea(new Java15Features.Circle(1_000_000.0));
            assertThat(area).isPositive().isGreaterThan(1e12);
        }
    }

    @Nested
    @DisplayName("Hidden Classes")
    class HiddenClassesTests {

        @Test
        @DisplayName("Should explain hidden classes")
        void testExplainHiddenClasses() {
            String explanation = Java15Features.explainHiddenClasses();
            assertThat(explanation).contains("Hidden Classes");
            assertThat(explanation).contains("defineHiddenClass");
        }

        @Test
        @DisplayName("Should create dynamic action")
        void testCreateDynamicAction() {
            Java15Features.DynamicAction action = Java15Features.createDynamicAction("PREFIX");
            assertThat(action.execute("test")).isEqualTo("PREFIX: test");
        }

        @Test
        @DisplayName("Should create multiple dynamic actions with different prefixes")
        void testMultipleDynamicActions() {
            Java15Features.DynamicAction action1 = Java15Features.createDynamicAction("LOG");
            Java15Features.DynamicAction action2 = Java15Features.createDynamicAction("ERROR");
            assertThat(action1.execute("msg")).isEqualTo("LOG: msg");
            assertThat(action2.execute("msg")).isEqualTo("ERROR: msg");
        }

        @Test
        @DisplayName("Should check non-hidden regular class")
        void testIsHiddenClassRegular() {
            assertThat(Java15Features.isHiddenClass(String.class)).isFalse();
            assertThat(Java15Features.isHiddenClass(Java15Features.class)).isFalse();
        }

        @Test
        @DisplayName("Should process list with lambda (uses hidden classes internally)")
        void testProcessWithLambda() {
            List<String> result = Java15Features.processWithLambda(
                    Arrays.asList("hello", null, "  ", "world", "", " test "));
            assertThat(result).containsExactly("HELLO", "WORLD", "TEST");
        }

        @Test
        @DisplayName("Should handle empty list in processWithLambda")
        void testProcessEmptyList() {
            assertThat(Java15Features.processWithLambda(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("Should handle all-null list")
        void testProcessAllNulls() {
            assertThat(Java15Features.processWithLambda(Arrays.asList(null, null))).isEmpty();
        }
    }
}
