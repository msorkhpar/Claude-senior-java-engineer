package com.github.msorkhpar.claudejavatutor.modernjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Java 21 Features Tests")
class Java21FeaturesTest {

    @Nested
    @DisplayName("Pattern Matching for switch")
    class PatternMatchingSwitchTests {

        @Test
        @DisplayName("Should format negative integer")
        void testFormatNegativeInteger() {
            assertThat(Java21Features.formatValue(-5)).isEqualTo("Negative integer: -5");
        }

        @Test
        @DisplayName("Should format positive integer")
        void testFormatPositiveInteger() {
            assertThat(Java21Features.formatValue(42)).isEqualTo("Positive integer: 42");
        }

        @Test
        @DisplayName("Should format zero as positive integer")
        void testFormatZeroInteger() {
            assertThat(Java21Features.formatValue(0)).isEqualTo("Positive integer: 0");
        }

        @Test
        @DisplayName("Should format empty string")
        void testFormatEmptyString() {
            assertThat(Java21Features.formatValue("")).isEqualTo("Empty string");
        }

        @Test
        @DisplayName("Should format non-empty string")
        void testFormatString() {
            assertThat(Java21Features.formatValue("hello")).isEqualTo("String: hello");
        }

        @Test
        @DisplayName("Should format double")
        void testFormatDouble() {
            assertThat(Java21Features.formatValue(3.14)).isEqualTo("Double: 3.14");
        }

        @Test
        @DisplayName("Should format empty list")
        void testFormatEmptyList() {
            assertThat(Java21Features.formatValue(List.of())).isEqualTo("Empty list");
        }

        @Test
        @DisplayName("Should format non-empty list")
        void testFormatNonEmptyList() {
            assertThat(Java21Features.formatValue(List.of(1, 2, 3)))
                    .isEqualTo("List with 3 elements");
        }

        @Test
        @DisplayName("Should format null")
        void testFormatNull() {
            assertThat(Java21Features.formatValue(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("Should format unknown type")
        void testFormatUnknown() {
            assertThat(Java21Features.formatValue(new Object())).startsWith("Unknown: Object");
        }

        @Test
        @DisplayName("Should classify numbers correctly")
        void testClassifyNumber() {
            assertThat(Java21Features.classifyNumber(0)).isEqualTo("zero");
            assertThat(Java21Features.classifyNumber(5)).isEqualTo("positive integer");
            assertThat(Java21Features.classifyNumber(-3)).isEqualTo("negative integer");
            assertThat(Java21Features.classifyNumber(Double.NaN)).isEqualTo("not a number");
            assertThat(Java21Features.classifyNumber(Double.POSITIVE_INFINITY)).isEqualTo("infinite");
            assertThat(Java21Features.classifyNumber(0.0)).isEqualTo("zero");
            assertThat(Java21Features.classifyNumber(3.14)).isEqualTo("positive double");
            assertThat(Java21Features.classifyNumber(-2.5)).isEqualTo("negative double");
            assertThat(Java21Features.classifyNumber(0L)).isEqualTo("zero");
            assertThat(Java21Features.classifyNumber(100L)).isEqualTo("positive long");
            assertThat(Java21Features.classifyNumber(-50L)).isEqualTo("negative long");
        }
    }

    @Nested
    @DisplayName("Exhaustive switch with Sealed Classes")
    class SealedSwitchTests {

        @Test
        @DisplayName("Should process email notification")
        void testEmailNotification() {
            var notification = new Java21Features.EmailNotification(
                    "user@example.com", "Hello", "Welcome!");
            assertThat(Java21Features.processNotification(notification))
                    .isEqualTo("Email to user@example.com: Hello");
        }

        @Test
        @DisplayName("Should process SMS notification")
        void testSmsNotification() {
            var notification = new Java21Features.SmsNotification(
                    "+1234567890", "Your code is 1234");
            assertThat(Java21Features.processNotification(notification))
                    .isEqualTo("SMS to +1234567890: Your code is 1234");
        }

        @Test
        @DisplayName("Should process push notification")
        void testPushNotification() {
            var notification = new Java21Features.PushNotification(
                    "device-123", "New Update", "{\"version\":\"2.0\"}");
            assertThat(Java21Features.processNotification(notification))
                    .isEqualTo("Push to device-123: New Update");
        }
    }

    @Nested
    @DisplayName("Record Patterns")
    class RecordPatternTests {

        @Test
        @DisplayName("Should describe origin point")
        void testOriginPoint() {
            assertThat(Java21Features.describePoint(new Java21Features.Point(0, 0)))
                    .isEqualTo("Origin");
        }

        @Test
        @DisplayName("Should describe point on Y-axis")
        void testPointOnYAxis() {
            assertThat(Java21Features.describePoint(new Java21Features.Point(0, 5)))
                    .isEqualTo("On Y-axis at y=5");
        }

        @Test
        @DisplayName("Should describe point on X-axis")
        void testPointOnXAxis() {
            assertThat(Java21Features.describePoint(new Java21Features.Point(3, 0)))
                    .isEqualTo("On X-axis at x=3");
        }

        @Test
        @DisplayName("Should describe arbitrary point")
        void testArbitraryPoint() {
            assertThat(Java21Features.describePoint(new Java21Features.Point(3, 4)))
                    .isEqualTo("Point at (3, 4)");
        }

        @Test
        @DisplayName("Should handle non-point object")
        void testNonPointObject() {
            assertThat(Java21Features.describePoint("not a point")).isEqualTo("Not a point");
        }

        @Test
        @DisplayName("Should describe degenerate line")
        void testDegenerateLine() {
            var line = new Java21Features.Line(
                    new Java21Features.Point(1, 1), new Java21Features.Point(1, 1));
            assertThat(Java21Features.describeLine(line))
                    .isEqualTo("Degenerate line (single point)");
        }

        @Test
        @DisplayName("Should describe vertical line")
        void testVerticalLine() {
            var line = new Java21Features.Line(
                    new Java21Features.Point(5, 0), new Java21Features.Point(5, 10));
            assertThat(Java21Features.describeLine(line))
                    .isEqualTo("Vertical line at x=5");
        }

        @Test
        @DisplayName("Should describe horizontal line")
        void testHorizontalLine() {
            var line = new Java21Features.Line(
                    new Java21Features.Point(0, 3), new Java21Features.Point(10, 3));
            assertThat(Java21Features.describeLine(line))
                    .isEqualTo("Horizontal line at y=3");
        }

        @Test
        @DisplayName("Should describe general line")
        void testGeneralLine() {
            var line = new Java21Features.Line(
                    new Java21Features.Point(1, 2), new Java21Features.Point(3, 4));
            assertThat(Java21Features.describeLine(line))
                    .isEqualTo("Line from (1,2) to (3,4)");
        }

        @Test
        @DisplayName("Should describe colored point")
        void testColoredPoint() {
            var cp = new Java21Features.ColoredPoint(
                    new Java21Features.Point(5, 10), "red");
            assertThat(Java21Features.describeColoredPoint(cp))
                    .isEqualTo("red point at (5, 10)");
        }

        @Test
        @DisplayName("Should handle non-colored-point object")
        void testNonColoredPoint() {
            assertThat(Java21Features.describeColoredPoint("string"))
                    .isEqualTo("Not a colored point");
        }
    }

    @Nested
    @DisplayName("Virtual Threads")
    class VirtualThreadTests {

        @Test
        @DisplayName("Should run task on virtual thread")
        void testRunOnVirtualThread() throws Exception {
            String result = Java21Features.runOnVirtualThread(() -> "Hello from virtual thread");
            assertThat(result).isEqualTo("Hello from virtual thread");
        }

        @Test
        @DisplayName("Should run many virtual threads concurrently")
        void testRunManyVirtualThreads() throws InterruptedException {
            List<Integer> results = Java21Features.runManyVirtualThreads(100);
            assertThat(results).hasSize(100);
            assertThat(results).containsExactlyElementsOf(
                    java.util.stream.IntStream.range(0, 100).boxed().toList());
        }

        @Test
        @DisplayName("Should execute tasks on virtual thread executor")
        void testExecuteTasksOnVirtualThreads() throws Exception {
            List<Callable<String>> tasks = List.of(
                    () -> "task1",
                    () -> "task2",
                    () -> "task3"
            );
            List<String> results = Java21Features.executeTasksOnVirtualThreads(tasks);
            assertThat(results).containsExactly("task1", "task2", "task3");
        }

        @Test
        @DisplayName("Should confirm virtual thread identity")
        void testIsVirtualThread() throws Exception {
            assertThat(Java21Features.isVirtualThread()).isTrue();
        }

        @Test
        @DisplayName("Should compare thread creation times")
        void testCompareThreadCreation() throws InterruptedException {
            Map<String, Long> results = Java21Features.compareThreadCreationTime(500);
            assertThat(results).containsKeys("virtualThreadsNanos", "platformThreadsNanos");
            assertThat(results.get("virtualCount")).isEqualTo(500L);
        }

        @Test
        @DisplayName("Should handle empty task list")
        void testEmptyTaskList() throws Exception {
            List<Callable<String>> tasks = Collections.emptyList();
            List<String> results = Java21Features.executeTasksOnVirtualThreads(tasks);
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Structured Concurrency Pattern")
    class StructuredConcurrencyTests {

        @Test
        @DisplayName("Should run structured tasks successfully")
        void testRunStructuredTasksSuccess() {
            List<Callable<String>> tasks = List.of(
                    () -> "result1",
                    () -> "result2"
            );
            List<Java21Features.TaskResult<String>> results =
                    Java21Features.runStructuredTasks(tasks);
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(r -> r.success());
            assertThat(results.get(0).value()).isEqualTo("result1");
            assertThat(results.get(1).value()).isEqualTo("result2");
        }

        @Test
        @DisplayName("Should handle task failures gracefully")
        void testRunStructuredTasksWithFailure() {
            List<Callable<String>> tasks = List.of(
                    () -> "success",
                    () -> { throw new RuntimeException("task failed"); }
            );
            List<Java21Features.TaskResult<String>> results =
                    Java21Features.runStructuredTasks(tasks);
            assertThat(results).hasSize(2);
            assertThat(results.get(0).success()).isTrue();
            assertThat(results.get(1).success()).isFalse();
            assertThat(results.get(1).error()).contains("task failed");
        }

        @Test
        @DisplayName("Should handle empty task list")
        void testRunStructuredTasksEmpty() {
            List<Java21Features.TaskResult<String>> results =
                    Java21Features.runStructuredTasks(Collections.emptyList());
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Foreign Function & Memory API")
    class FFMApiTests {

        @Test
        @DisplayName("Should explain FFM API")
        void testExplainFFMApi() {
            String explanation = Java21Features.explainFFMApi();
            assertThat(explanation).contains("Foreign Function & Memory API");
            assertThat(explanation).contains("MemorySegment");
            assertThat(explanation).contains("Linker");
        }
    }

    @Nested
    @DisplayName("Sequenced Collections")
    class SequencedCollectionTests {

        @Test
        @DisplayName("Should get first element")
        void testGetFirst() {
            LinkedList<String> list = new LinkedList<>(List.of("a", "b", "c"));
            assertThat(Java21Features.getFirst(list)).isEqualTo("a");
        }

        @Test
        @DisplayName("Should get last element")
        void testGetLast() {
            LinkedList<String> list = new LinkedList<>(List.of("a", "b", "c"));
            assertThat(Java21Features.getLast(list)).isEqualTo("c");
        }

        @Test
        @DisplayName("Should throw on empty collection getFirst")
        void testGetFirstEmpty() {
            assertThatThrownBy(() -> Java21Features.getFirst(new LinkedList<>()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Should throw on empty collection getLast")
        void testGetLastEmpty() {
            assertThatThrownBy(() -> Java21Features.getLast(new LinkedList<>()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Should reverse sequenced collection")
        void testReversed() {
            LinkedList<String> list = new LinkedList<>(List.of("a", "b", "c"));
            var reversed = Java21Features.reversed(list);
            assertThat(reversed).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("String Formatting")
    class StringFormattingTests {

        @Test
        @DisplayName("Should format greeting")
        void testFormatGreeting() {
            assertThat(Java21Features.formatGreeting("Alice", 30))
                    .isEqualTo("Hello, Alice! You are 30 years old.");
        }

        @Test
        @DisplayName("Should handle empty name")
        void testFormatGreetingEmptyName() {
            assertThat(Java21Features.formatGreeting("", 25))
                    .isEqualTo("Hello, ! You are 25 years old.");
        }
    }
}
