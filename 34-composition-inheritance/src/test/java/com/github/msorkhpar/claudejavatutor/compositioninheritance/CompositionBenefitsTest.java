package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Composition Benefits Tests (8.4.3)")
class CompositionBenefitsTest {

    // ---- SimpleCache ----

    @Nested
    @DisplayName("SimpleCache")
    class SimpleCacheTest {

        @Test
        @DisplayName("Should put and get values")
        void testPutAndGet() {
            var cache = new CompositionBenefits.SimpleCache<String, Integer>();
            cache.put("key", 42);

            assertThat(cache.get("key")).isEqualTo(42);
        }

        @Test
        @DisplayName("Should return null for missing key")
        void testMissingKey() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();

            assertThat(cache.get("missing")).isNull();
        }

        @Test
        @DisplayName("Should overwrite existing key")
        void testOverwrite() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();
            cache.put("key", "old");
            cache.put("key", "new");

            assertThat(cache.get("key")).isEqualTo("new");
            assertThat(cache.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reject null key")
        void testNullKey() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();

            assertThatThrownBy(() -> cache.put(null, "value"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Key must not be null");
        }

        @Test
        @DisplayName("Should clear all entries")
        void testClear() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();
            cache.put("a", "1");
            cache.put("b", "2");
            cache.clear();

            assertThat(cache.size()).isEqualTo(0);
            assertThat(cache.get("a")).isNull();
        }

        @Test
        @DisplayName("Should report containsKey correctly")
        void testContainsKey() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();
            cache.put("exists", "value");

            assertThat(cache.containsKey("exists")).isTrue();
            assertThat(cache.containsKey("missing")).isFalse();
        }

        @Test
        @DisplayName("Should allow null values")
        void testNullValue() {
            var cache = new CompositionBenefits.SimpleCache<String, String>();
            cache.put("key", null);

            assertThat(cache.containsKey("key")).isTrue();
            assertThat(cache.get("key")).isNull();
        }
    }

    // ---- ThreadSafeCache ----

    @Nested
    @DisplayName("ThreadSafeCache")
    class ThreadSafeCacheTest {

        @Test
        @DisplayName("Should wrap SimpleCache and provide thread-safe access")
        void testBasicOperations() {
            var cache = new CompositionBenefits.ThreadSafeCache<>(new CompositionBenefits.SimpleCache<String, Integer>());
            cache.put("count", 10);

            assertThat(cache.get("count")).isEqualTo(10);
            assertThat(cache.size()).isEqualTo(1);
            assertThat(cache.containsKey("count")).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent puts safely")
        void testConcurrentPuts() throws Exception {
            var cache = new CompositionBenefits.ThreadSafeCache<>(new CompositionBenefits.SimpleCache<Integer, String>());
            int threadCount = 50;
            var latch = new CountDownLatch(threadCount);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < threadCount; i++) {
                    final int key = i;
                    executor.submit(() -> {
                        cache.put(key, "value-" + key);
                        latch.countDown();
                    });
                }
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(cache.size()).isEqualTo(threadCount);
        }

        @Test
        @DisplayName("Should handle concurrent reads and writes")
        void testConcurrentReadsAndWrites() throws Exception {
            var cache = new CompositionBenefits.ThreadSafeCache<>(new CompositionBenefits.SimpleCache<String, Integer>());
            cache.put("counter", 0);

            int iterations = 100;
            var latch = new CountDownLatch(iterations * 2);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < iterations; i++) {
                    final int val = i;
                    executor.submit(() -> {
                        cache.put("key-" + val, val);
                        latch.countDown();
                    });
                    executor.submit(() -> {
                        cache.get("counter");
                        latch.countDown();
                    });
                }
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(cache.containsKey("counter")).isTrue();
        }

        @Test
        @DisplayName("Should reject null delegate")
        void testNullDelegate() {
            assertThatThrownBy(() -> new CompositionBenefits.ThreadSafeCache<>(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Delegate cache must not be null");
        }

        @Test
        @DisplayName("Should clear all entries")
        void testClear() {
            var cache = new CompositionBenefits.ThreadSafeCache<>(new CompositionBenefits.SimpleCache<String, String>());
            cache.put("a", "1");
            cache.put("b", "2");
            cache.clear();

            assertThat(cache.size()).isEqualTo(0);
        }
    }

    // ---- BoundedCache ----

    @Nested
    @DisplayName("BoundedCache")
    class BoundedCacheTest {

        @Test
        @DisplayName("Should store up to maxSize entries")
        void testMaxSize() {
            var cache = new CompositionBenefits.BoundedCache<>(new CompositionBenefits.SimpleCache<String, String>(), 3);
            cache.put("a", "1");
            cache.put("b", "2");
            cache.put("c", "3");

            assertThat(cache.size()).isEqualTo(3);
            assertThat(cache.getMaxSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should reject non-positive maxSize")
        void testInvalidMaxSize() {
            assertThatThrownBy(() -> new CompositionBenefits.BoundedCache<>(new CompositionBenefits.SimpleCache<>(), 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("maxSize must be > 0");
        }

        @Test
        @DisplayName("Should reject null delegate")
        void testNullDelegate() {
            assertThatThrownBy(() -> new CompositionBenefits.BoundedCache<>(null, 10))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should clear all entries and access order")
        void testClear() {
            var cache = new CompositionBenefits.BoundedCache<>(new CompositionBenefits.SimpleCache<String, String>(), 5);
            cache.put("a", "1");
            cache.put("b", "2");
            cache.clear();

            assertThat(cache.size()).isEqualTo(0);
        }
    }

    // ---- NotificationService (modularity and testability) ----

    @Nested
    @DisplayName("NotificationService")
    class NotificationServiceTest {

        @Test
        @DisplayName("Should send formatted notification")
        void testSendFormattedNotification() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();
            var formatter = new CompositionBenefits.TemplateFormatter();
            var service = new CompositionBenefits.NotificationService(sender, formatter);

            boolean result = service.notify("user@test.com", "Hello ${name}!",
                    Map.of("name", "Alice"));

            assertThat(result).isTrue();
            assertThat(sender.getSentMessages()).containsExactly("user@test.com: Hello Alice!");
            assertThat(service.getSuccessCount()).isEqualTo(1);
            assertThat(service.getFailureCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should track failure count for invalid recipients")
        void testFailedNotification() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();
            var formatter = new CompositionBenefits.TemplateFormatter();
            var service = new CompositionBenefits.NotificationService(sender, formatter);

            boolean result = service.notify("", "Hello!", Map.of());

            assertThat(result).isFalse();
            assertThat(service.getFailureCount()).isEqualTo(1);
            assertThat(service.getSuccessCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should reject null sender")
        void testNullSender() {
            assertThatThrownBy(() -> new CompositionBenefits.NotificationService(
                    null, new CompositionBenefits.TemplateFormatter()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Sender must not be null");
        }

        @Test
        @DisplayName("Should reject null formatter")
        void testNullFormatter() {
            assertThatThrownBy(() -> new CompositionBenefits.NotificationService(
                    new CompositionBenefits.InMemoryNotificationSender(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Formatter must not be null");
        }

        @Test
        @DisplayName("Should handle template with multiple variables")
        void testMultipleVariables() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();
            var formatter = new CompositionBenefits.TemplateFormatter();
            var service = new CompositionBenefits.NotificationService(sender, formatter);

            service.notify("admin@test.com", "User ${user} performed ${action}",
                    Map.of("user", "Bob", "action", "login"));

            assertThat(sender.getSentMessages())
                    .containsExactly("admin@test.com: User Bob performed login");
        }

        @Test
        @DisplayName("Should handle template without variables")
        void testNoVariables() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();
            var formatter = new CompositionBenefits.TemplateFormatter();
            var service = new CompositionBenefits.NotificationService(sender, formatter);

            service.notify("user@test.com", "Plain message", Map.of());

            assertThat(sender.getSentMessages()).containsExactly("user@test.com: Plain message");
        }
    }

    // ---- TemplateFormatter ----

    @Nested
    @DisplayName("TemplateFormatter")
    class TemplateFormatterTest {

        @Test
        @DisplayName("Should replace placeholders with values")
        void testReplacePlaceholders() {
            var formatter = new CompositionBenefits.TemplateFormatter();

            String result = formatter.format("Hello ${name}, welcome to ${place}!",
                    Map.of("name", "Alice", "place", "Wonderland"));

            assertThat(result).isEqualTo("Hello Alice, welcome to Wonderland!");
        }

        @Test
        @DisplayName("Should return template as-is when no variables provided")
        void testNoVariables() {
            var formatter = new CompositionBenefits.TemplateFormatter();

            assertThat(formatter.format("No vars", Map.of())).isEqualTo("No vars");
            assertThat(formatter.format("No vars", null)).isEqualTo("No vars");
        }

        @Test
        @DisplayName("Should return null for null template")
        void testNullTemplate() {
            var formatter = new CompositionBenefits.TemplateFormatter();

            assertThat(formatter.format(null, Map.of("key", "val"))).isNull();
        }

        @Test
        @DisplayName("Should leave unmatched placeholders")
        void testUnmatchedPlaceholders() {
            var formatter = new CompositionBenefits.TemplateFormatter();

            String result = formatter.format("Hello ${name}, age ${age}",
                    Map.of("name", "Bob"));

            assertThat(result).isEqualTo("Hello Bob, age ${age}");
        }
    }

    // ---- InMemoryNotificationSender ----

    @Nested
    @DisplayName("InMemoryNotificationSender")
    class InMemoryNotificationSenderTest {

        @Test
        @DisplayName("Should send message and store it")
        void testSendAndStore() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();

            assertThat(sender.send("user", "hello")).isTrue();
            assertThat(sender.getSentMessages()).containsExactly("user: hello");
        }

        @Test
        @DisplayName("Should reject null recipient")
        void testNullRecipient() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();

            assertThat(sender.send(null, "hello")).isFalse();
        }

        @Test
        @DisplayName("Should reject blank recipient")
        void testBlankRecipient() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();

            assertThat(sender.send("  ", "hello")).isFalse();
        }

        @Test
        @DisplayName("Should reject null message")
        void testNullMessage() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();

            assertThat(sender.send("user", null)).isFalse();
        }

        @Test
        @DisplayName("Should reject blank message")
        void testBlankMessage() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();

            assertThat(sender.send("user", "")).isFalse();
        }

        @Test
        @DisplayName("Should return unmodifiable messages list")
        void testUnmodifiableList() {
            var sender = new CompositionBenefits.InMemoryNotificationSender();
            sender.send("user", "msg");

            assertThatThrownBy(() -> sender.getSentMessages().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ---- ProcessingPipeline ----

    @Nested
    @DisplayName("ProcessingPipeline")
    class ProcessingPipelineTest {

        @Test
        @DisplayName("Should execute stages in order")
        void testStageOrder() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>()
                    .addStage(String::trim)
                    .addStage(String::toUpperCase)
                    .addStage(s -> s + "!");

            assertThat(pipeline.execute("  hello  ")).isEqualTo("HELLO!");
        }

        @Test
        @DisplayName("Should return input for empty pipeline")
        void testEmptyPipeline() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>();

            assertThat(pipeline.execute("unchanged")).isEqualTo("unchanged");
            assertThat(pipeline.stageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return null if any stage returns null")
        void testNullShortCircuit() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>()
                    .addStage(s -> null)
                    .addStage(String::toUpperCase);

            assertThat(pipeline.execute("test")).isNull();
        }

        @Test
        @DisplayName("Should reject null stage")
        void testNullStage() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>();

            assertThatThrownBy(() -> pipeline.addStage(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Stage must not be null");
        }

        @Test
        @DisplayName("Should track stage count")
        void testStageCount() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<Integer>()
                    .addStage(n -> n + 1)
                    .addStage(n -> n * 2);

            assertThat(pipeline.stageCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should process integer pipeline correctly")
        void testIntegerPipeline() {
            var pipeline = new CompositionBenefits.ProcessingPipeline<Integer>()
                    .addStage(n -> n + 10)
                    .addStage(n -> n * 2);

            assertThat(pipeline.execute(5)).isEqualTo(30); // (5+10)*2 = 30
        }
    }

    // ---- ConcurrentPipelineExecutor ----

    @Nested
    @DisplayName("ConcurrentPipelineExecutor")
    class ConcurrentPipelineExecutorTest {

        @Test
        @DisplayName("Should process items concurrently with virtual threads")
        void testConcurrentProcessing() throws Exception {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>()
                    .addStage(String::toUpperCase)
                    .addStage(s -> s + "!");

            var executor = new CompositionBenefits.ConcurrentPipelineExecutor<>(pipeline);
            List<String> results = executor.processAll(List.of("hello", "world", "java"));

            assertThat(results).containsExactly("HELLO!", "WORLD!", "JAVA!");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testEmptyList() throws Exception {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>()
                    .addStage(String::toUpperCase);

            var executor = new CompositionBenefits.ConcurrentPipelineExecutor<>(pipeline);
            List<String> results = executor.processAll(Collections.emptyList());

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle null list")
        void testNullList() throws Exception {
            var pipeline = new CompositionBenefits.ProcessingPipeline<String>()
                    .addStage(String::toUpperCase);

            var executor = new CompositionBenefits.ConcurrentPipelineExecutor<>(pipeline);
            List<String> results = executor.processAll(null);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should reject null pipeline")
        void testNullPipeline() {
            assertThatThrownBy(() -> new CompositionBenefits.ConcurrentPipelineExecutor<>(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Pipeline must not be null");
        }

        @Test
        @DisplayName("Should process many items concurrently")
        void testManyItems() throws Exception {
            var pipeline = new CompositionBenefits.ProcessingPipeline<Integer>()
                    .addStage(n -> n * 2);

            var executor = new CompositionBenefits.ConcurrentPipelineExecutor<>(pipeline);
            List<Integer> input = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                input.add(i);
            }

            List<Integer> results = executor.processAll(input);

            assertThat(results).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(results.get(i)).isEqualTo(i * 2);
            }
        }
    }
}
