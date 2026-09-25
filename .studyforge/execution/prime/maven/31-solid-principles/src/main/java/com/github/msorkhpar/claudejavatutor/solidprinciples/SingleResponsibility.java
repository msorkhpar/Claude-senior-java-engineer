package com.github.msorkhpar.claudejavatutor.solidprinciples;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Demonstrates the Single Responsibility Principle (SRP).
 * Each class should have only one reason to change.
 */
public class SingleResponsibility {

    // ========== VIOLATION EXAMPLE ==========

    /**
     * Violates SRP: This class handles user data, persistence, AND notification.
     * It has three reasons to change.
     */
    public static class UserServiceViolation {
        private final Map<String, String> users = new ConcurrentHashMap<>();
        private final List<String> notifications = new CopyOnWriteArrayList<>();

        public void createUser(String id, String name) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("User ID cannot be null or blank");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("User name cannot be null or blank");
            }
            users.put(id, name);
            notifications.add("User created: " + name);
        }

        public String findUser(String id) {
            return users.get(id);
        }

        public List<String> getNotifications() {
            return Collections.unmodifiableList(notifications);
        }

        public int getUserCount() {
            return users.size();
        }
    }

    // ========== CORRECT EXAMPLE: SRP Applied ==========

    /**
     * Responsibility 1: User data management only.
     */
    public static class UserRepository {
        private final Map<String, String> users = new ConcurrentHashMap<>();

        public void save(String id, String name) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("User ID cannot be null or blank");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("User name cannot be null or blank");
            }
            users.put(id, name);
        }

        public Optional<String> findById(String id) {
            return Optional.ofNullable(users.get(id));
        }

        public List<String> findAll() {
            return List.copyOf(users.values());
        }

        public int count() {
            return users.size();
        }

        public boolean exists(String id) {
            return users.containsKey(id);
        }
    }

    /**
     * Responsibility 2: Notification handling only.
     */
    public static class NotificationService {
        private final List<String> sentNotifications = new CopyOnWriteArrayList<>();

        public void sendNotification(String message) {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Notification message cannot be null or blank");
            }
            sentNotifications.add(message);
        }

        public List<String> getSentNotifications() {
            return Collections.unmodifiableList(sentNotifications);
        }

        public int getNotificationCount() {
            return sentNotifications.size();
        }
    }

    /**
     * Responsibility 3: Orchestration / use case coordination.
     */
    public static class UserService {
        private final UserRepository repository;
        private final NotificationService notificationService;

        public UserService(UserRepository repository, NotificationService notificationService) {
            this.repository = Objects.requireNonNull(repository);
            this.notificationService = Objects.requireNonNull(notificationService);
        }

        public void createUser(String id, String name) {
            repository.save(id, name);
            notificationService.sendNotification("User created: " + name);
        }

        public Optional<String> findUser(String id) {
            return repository.findById(id);
        }
    }

    // ========== SRP IN CONCURRENT PROGRAMMING ==========

    /**
     * Demonstrates SRP in a concurrent context: separate concerns
     * for task production, execution, and result aggregation.
     */
    public static class TaskProducer {
        public List<Callable<Long>> createTasks(int count) {
            if (count < 0) {
                throw new IllegalArgumentException("Count must be non-negative");
            }
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final int taskId = i;
                tasks.add(() -> (long) taskId * taskId);
            }
            return tasks;
        }
    }

    /**
     * Single responsibility: executing tasks in a thread pool.
     */
    public static class TaskExecutor {
        private final ExecutorService executorService;

        public TaskExecutor(ExecutorService executorService) {
            this.executorService = Objects.requireNonNull(executorService);
        }

        public List<Future<Long>> submitAll(List<Callable<Long>> tasks) {
            if (tasks == null) {
                throw new IllegalArgumentException("Tasks list cannot be null");
            }
            return tasks.stream()
                    .map(executorService::submit)
                    .collect(Collectors.toList());
        }

        public void shutdown() {
            executorService.shutdown();
        }
    }

    /**
     * Single responsibility: aggregating results from futures.
     */
    public static class ResultAggregator {
        public long sumResults(List<Future<Long>> futures) throws ExecutionException, InterruptedException {
            if (futures == null) {
                throw new IllegalArgumentException("Futures list cannot be null");
            }
            long sum = 0;
            for (Future<Long> future : futures) {
                sum += future.get();
            }
            return sum;
        }
    }

    // ========== SRP FOR THREAD-SAFE COUNTER ==========

    /**
     * Single responsibility: thread-safe counting only.
     */
    public static class ThreadSafeCounter {
        private final AtomicLong count = new AtomicLong(0);

        public long increment() {
            return count.incrementAndGet();
        }

        public long decrement() {
            return count.decrementAndGet();
        }

        public long getCount() {
            return count.get();
        }

        public void reset() {
            count.set(0);
        }
    }

    /**
     * Single responsibility: formatting counter values for display.
     */
    public static class CounterFormatter {
        public String format(long count) {
            if (count < 0) {
                return "Deficit: " + Math.abs(count);
            } else if (count == 0) {
                return "Zero";
            } else {
                return "Count: " + count;
            }
        }
    }
}
