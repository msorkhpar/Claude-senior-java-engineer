package com.github.msorkhpar.claudejavatutor.solidprinciples;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the Dependency Inversion Principle (DIP).
 * High-level modules should not depend on low-level modules.
 * Both should depend on abstractions.
 */
public class DependencyInversion {

    // ========== VIOLATION EXAMPLE ==========

    /**
     * Low-level module: concrete email sender.
     */
    public static class EmailSenderViolation {
        private final List<String> sentEmails = new ArrayList<>();

        public void sendEmail(String to, String message) {
            sentEmails.add("Email to " + to + ": " + message);
        }

        public List<String> getSentEmails() {
            return Collections.unmodifiableList(sentEmails);
        }
    }

    /**
     * Violates DIP: High-level module directly depends on low-level concrete class.
     * Cannot switch to SMS, push notification, etc., without modifying this class.
     */
    public static class NotificationManagerViolation {
        private final EmailSenderViolation emailSender = new EmailSenderViolation();

        public void notifyUser(String user, String message) {
            emailSender.sendEmail(user, message);
        }

        public List<String> getSentNotifications() {
            return emailSender.getSentEmails();
        }
    }

    // ========== CORRECT EXAMPLE: DIP Applied ==========

    /**
     * Abstraction that both high-level and low-level modules depend on.
     */
    public interface MessageSender {
        void send(String recipient, String message);
        List<String> getSentMessages();
    }

    /**
     * Low-level module 1: Email implementation of MessageSender.
     */
    public static class EmailSender implements MessageSender {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        @Override
        public void send(String recipient, String message) {
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalArgumentException("Recipient cannot be null or blank");
            }
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Message cannot be null or blank");
            }
            sent.add("EMAIL[%s]: %s".formatted(recipient, message));
        }

        @Override
        public List<String> getSentMessages() {
            return Collections.unmodifiableList(sent);
        }
    }

    /**
     * Low-level module 2: SMS implementation of MessageSender.
     */
    public static class SmsSender implements MessageSender {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        @Override
        public void send(String recipient, String message) {
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalArgumentException("Recipient cannot be null or blank");
            }
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Message cannot be null or blank");
            }
            sent.add("SMS[%s]: %s".formatted(recipient, message));
        }

        @Override
        public List<String> getSentMessages() {
            return Collections.unmodifiableList(sent);
        }
    }

    /**
     * High-level module: depends on abstraction, not concrete implementation.
     */
    public static class NotificationManager {
        private final MessageSender sender;

        public NotificationManager(MessageSender sender) {
            this.sender = Objects.requireNonNull(sender, "MessageSender cannot be null");
        }

        public void notifyUser(String user, String message) {
            sender.send(user, message);
        }

        public List<String> getSentNotifications() {
            return sender.getSentMessages();
        }
    }

    // ========== DIP IN CONCURRENT PROGRAMMING ==========

    /**
     * Abstraction for task execution strategy.
     */
    public interface ExecutionStrategy {
        <T> Future<T> execute(Callable<T> task);
        void shutdown();
    }

    /**
     * Concrete strategy: uses a fixed thread pool.
     */
    public static class FixedPoolStrategy implements ExecutionStrategy {
        private final ExecutorService executor;

        public FixedPoolStrategy(int poolSize) {
            if (poolSize <= 0) throw new IllegalArgumentException("Pool size must be positive");
            this.executor = Executors.newFixedThreadPool(poolSize);
        }

        @Override
        public <T> Future<T> execute(Callable<T> task) {
            if (task == null) throw new NullPointerException("Task cannot be null");
            return executor.submit(task);
        }

        @Override
        public void shutdown() {
            executor.shutdown();
        }
    }

    /**
     * Concrete strategy: uses virtual threads.
     */
    public static class VirtualThreadStrategy implements ExecutionStrategy {
        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        @Override
        public <T> Future<T> execute(Callable<T> task) {
            if (task == null) throw new NullPointerException("Task cannot be null");
            return executor.submit(task);
        }

        @Override
        public void shutdown() {
            executor.shutdown();
        }
    }

    /**
     * High-level module: job scheduler that depends on ExecutionStrategy abstraction.
     * Can swap between platform threads and virtual threads without modification.
     */
    public static class JobScheduler {
        private final ExecutionStrategy strategy;
        private final AtomicInteger jobCount = new AtomicInteger(0);

        public JobScheduler(ExecutionStrategy strategy) {
            this.strategy = Objects.requireNonNull(strategy, "ExecutionStrategy cannot be null");
        }

        public <T> Future<T> scheduleJob(Callable<T> job) {
            if (job == null) throw new NullPointerException("Job cannot be null");
            jobCount.incrementAndGet();
            return strategy.execute(job);
        }

        public int getJobCount() {
            return jobCount.get();
        }

        public void shutdown() {
            strategy.shutdown();
        }
    }

    // ========== DIP AND TESTABILITY ==========

    /**
     * Abstraction for a data repository.
     */
    public interface DataRepository {
        void save(String key, String value);
        Optional<String> findByKey(String key);
        List<String> findAll();
    }

    /**
     * In-memory implementation for testing.
     */
    public static class InMemoryRepository implements DataRepository {
        private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

        @Override
        public void save(String key, String value) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            if (value == null) throw new NullPointerException("Value cannot be null");
            store.put(key, value);
        }

        @Override
        public Optional<String> findByKey(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public List<String> findAll() {
            return List.copyOf(store.values());
        }
    }

    /**
     * Abstraction for a logger.
     */
    public interface Logger {
        void log(String message);
        List<String> getLogEntries();
    }

    /**
     * In-memory logger for testing.
     */
    public static class InMemoryLogger implements Logger {
        private final List<String> entries = new CopyOnWriteArrayList<>();

        @Override
        public void log(String message) {
            if (message == null) throw new NullPointerException("Log message cannot be null");
            entries.add(message);
        }

        @Override
        public List<String> getLogEntries() {
            return Collections.unmodifiableList(entries);
        }
    }

    /**
     * High-level business service: depends on abstractions for both persistence and logging.
     * Fully testable with in-memory implementations.
     */
    public static class BusinessService {
        private final DataRepository repository;
        private final Logger logger;

        public BusinessService(DataRepository repository, Logger logger) {
            this.repository = Objects.requireNonNull(repository, "DataRepository cannot be null");
            this.logger = Objects.requireNonNull(logger, "Logger cannot be null");
        }

        public void processData(String key, String value) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Key cannot be null or blank");
            }
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Value cannot be null or blank");
            }
            repository.save(key, value);
            logger.log("Processed: " + key);
        }

        public Optional<String> getData(String key) {
            logger.log("Retrieving: " + key);
            return repository.findByKey(key);
        }

        public List<String> getAllData() {
            logger.log("Listing all data");
            return repository.findAll();
        }
    }
}
