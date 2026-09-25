package com.github.msorkhpar.claudejavatutor.solidprinciples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Single Responsibility Principle Tests")
class SingleResponsibilityTest {

    @Nested
    @DisplayName("SRP Violation Example")
    class ViolationTest {

        @Test
        @DisplayName("Violation class handles multiple responsibilities")
        void testViolationClassHandlesMultipleResponsibilities() {
            var service = new SingleResponsibility.UserServiceViolation();
            service.createUser("1", "Alice");

            assertThat(service.findUser("1")).isEqualTo("Alice");
            assertThat(service.getNotifications()).containsExactly("User created: Alice");
            assertThat(service.getUserCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Violation class throws on null ID")
        void testViolationThrowsOnNullId() {
            var service = new SingleResponsibility.UserServiceViolation();
            assertThatThrownBy(() -> service.createUser(null, "Alice"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Violation class throws on blank name")
        void testViolationThrowsOnBlankName() {
            var service = new SingleResponsibility.UserServiceViolation();
            assertThatThrownBy(() -> service.createUser("1", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("UserRepository - Single Responsibility")
    class UserRepositoryTest {

        @Test
        @DisplayName("Should save and find user")
        void testSaveAndFind() {
            var repo = new SingleResponsibility.UserRepository();
            repo.save("1", "Alice");

            assertThat(repo.findById("1")).contains("Alice");
        }

        @Test
        @DisplayName("Should return empty for missing user")
        void testFindMissing() {
            var repo = new SingleResponsibility.UserRepository();
            assertThat(repo.findById("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("Should list all users")
        void testFindAll() {
            var repo = new SingleResponsibility.UserRepository();
            repo.save("1", "Alice");
            repo.save("2", "Bob");

            assertThat(repo.findAll()).containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        @DisplayName("Should count users")
        void testCount() {
            var repo = new SingleResponsibility.UserRepository();
            assertThat(repo.count()).isZero();
            repo.save("1", "Alice");
            assertThat(repo.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should check existence")
        void testExists() {
            var repo = new SingleResponsibility.UserRepository();
            repo.save("1", "Alice");
            assertThat(repo.exists("1")).isTrue();
            assertThat(repo.exists("2")).isFalse();
        }

        @Test
        @DisplayName("Should reject null ID")
        void testNullId() {
            var repo = new SingleResponsibility.UserRepository();
            assertThatThrownBy(() -> repo.save(null, "Alice"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank name")
        void testBlankName() {
            var repo = new SingleResponsibility.UserRepository();
            assertThatThrownBy(() -> repo.save("1", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("NotificationService - Single Responsibility")
    class NotificationServiceTest {

        @Test
        @DisplayName("Should send and retrieve notifications")
        void testSendAndRetrieve() {
            var service = new SingleResponsibility.NotificationService();
            service.sendNotification("Hello");

            assertThat(service.getSentNotifications()).containsExactly("Hello");
            assertThat(service.getNotificationCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reject null message")
        void testNullMessage() {
            var service = new SingleResponsibility.NotificationService();
            assertThatThrownBy(() -> service.sendNotification(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank message")
        void testBlankMessage() {
            var service = new SingleResponsibility.NotificationService();
            assertThatThrownBy(() -> service.sendNotification("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return unmodifiable notification list")
        void testUnmodifiableList() {
            var service = new SingleResponsibility.NotificationService();
            service.sendNotification("Test");
            assertThatThrownBy(() -> service.getSentNotifications().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("UserService - Orchestration")
    class UserServiceTest {

        @Test
        @DisplayName("Should coordinate repository and notification service")
        void testCreateUserCoordinates() {
            var repo = new SingleResponsibility.UserRepository();
            var notifier = new SingleResponsibility.NotificationService();
            var service = new SingleResponsibility.UserService(repo, notifier);

            service.createUser("1", "Alice");

            assertThat(service.findUser("1")).contains("Alice");
            assertThat(notifier.getSentNotifications()).containsExactly("User created: Alice");
        }

        @Test
        @DisplayName("Should reject null repository")
        void testNullRepository() {
            assertThatThrownBy(() -> new SingleResponsibility.UserService(null, new SingleResponsibility.NotificationService()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null notification service")
        void testNullNotificationService() {
            assertThatThrownBy(() -> new SingleResponsibility.UserService(new SingleResponsibility.UserRepository(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Concurrent SRP - TaskProducer")
    class TaskProducerTest {

        @Test
        @DisplayName("Should create specified number of tasks")
        void testCreateTasks() {
            var producer = new SingleResponsibility.TaskProducer();
            List<Callable<Long>> tasks = producer.createTasks(5);
            assertThat(tasks).hasSize(5);
        }

        @Test
        @DisplayName("Should create zero tasks")
        void testCreateZeroTasks() {
            var producer = new SingleResponsibility.TaskProducer();
            assertThat(producer.createTasks(0)).isEmpty();
        }

        @Test
        @DisplayName("Should reject negative count")
        void testNegativeCount() {
            var producer = new SingleResponsibility.TaskProducer();
            assertThatThrownBy(() -> producer.createTasks(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Tasks should compute squares")
        void testTasksComputeSquares() throws Exception {
            var producer = new SingleResponsibility.TaskProducer();
            List<Callable<Long>> tasks = producer.createTasks(4);

            List<Long> results = new ArrayList<>();
            for (Callable<Long> task : tasks) {
                results.add(task.call());
            }
            assertThat(results).containsExactly(0L, 1L, 4L, 9L);
        }
    }

    @Nested
    @DisplayName("Concurrent SRP - TaskExecutor")
    class TaskExecutorTest {

        @Test
        @DisplayName("Should submit all tasks")
        void testSubmitAll() throws Exception {
            ExecutorService exec = Executors.newFixedThreadPool(2);
            var executor = new SingleResponsibility.TaskExecutor(exec);

            List<Callable<Long>> tasks = List.of(() -> 1L, () -> 2L, () -> 3L);
            List<Future<Long>> futures = executor.submitAll(tasks);

            assertThat(futures).hasSize(3);
            List<Long> results = new ArrayList<>();
            for (Future<Long> f : futures) {
                results.add(f.get());
            }
            assertThat(results).containsExactly(1L, 2L, 3L);
            executor.shutdown();
        }

        @Test
        @DisplayName("Should reject null task list")
        void testNullTasks() {
            ExecutorService exec = Executors.newSingleThreadExecutor();
            var executor = new SingleResponsibility.TaskExecutor(exec);
            assertThatThrownBy(() -> executor.submitAll(null))
                    .isInstanceOf(IllegalArgumentException.class);
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Concurrent SRP - ResultAggregator")
    class ResultAggregatorTest {

        @Test
        @DisplayName("Should sum results from futures")
        void testSumResults() throws Exception {
            var aggregator = new SingleResponsibility.ResultAggregator();
            ExecutorService exec = Executors.newFixedThreadPool(2);

            List<Future<Long>> futures = List.of(
                    exec.submit(() -> 10L),
                    exec.submit(() -> 20L),
                    exec.submit(() -> 30L)
            );

            assertThat(aggregator.sumResults(futures)).isEqualTo(60L);
            exec.shutdown();
        }

        @Test
        @DisplayName("Should return zero for empty futures")
        void testEmptyFutures() throws Exception {
            var aggregator = new SingleResponsibility.ResultAggregator();
            assertThat(aggregator.sumResults(List.of())).isZero();
        }

        @Test
        @DisplayName("Should reject null futures list")
        void testNullFutures() {
            var aggregator = new SingleResponsibility.ResultAggregator();
            assertThatThrownBy(() -> aggregator.sumResults(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ThreadSafeCounter")
    class ThreadSafeCounterTest {

        @Test
        @DisplayName("Should increment and decrement")
        void testIncrementDecrement() {
            var counter = new SingleResponsibility.ThreadSafeCounter();
            assertThat(counter.increment()).isEqualTo(1);
            assertThat(counter.increment()).isEqualTo(2);
            assertThat(counter.decrement()).isEqualTo(1);
            assertThat(counter.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reset to zero")
        void testReset() {
            var counter = new SingleResponsibility.ThreadSafeCounter();
            counter.increment();
            counter.increment();
            counter.reset();
            assertThat(counter.getCount()).isZero();
        }

        @Test
        @DisplayName("Should handle concurrent increments")
        void testConcurrentIncrements() throws Exception {
            var counter = new SingleResponsibility.ThreadSafeCounter();
            int threads = 100;
            var latch = new CountDownLatch(threads);
            var executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    counter.increment();
                    latch.countDown();
                });
            }
            latch.await(5, TimeUnit.SECONDS);

            assertThat(counter.getCount()).isEqualTo(threads);
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("CounterFormatter")
    class CounterFormatterTest {

        @Test
        @DisplayName("Should format positive count")
        void testPositive() {
            var formatter = new SingleResponsibility.CounterFormatter();
            assertThat(formatter.format(42)).isEqualTo("Count: 42");
        }

        @Test
        @DisplayName("Should format zero")
        void testZero() {
            var formatter = new SingleResponsibility.CounterFormatter();
            assertThat(formatter.format(0)).isEqualTo("Zero");
        }

        @Test
        @DisplayName("Should format negative count")
        void testNegative() {
            var formatter = new SingleResponsibility.CounterFormatter();
            assertThat(formatter.format(-5)).isEqualTo("Deficit: 5");
        }
    }
}
