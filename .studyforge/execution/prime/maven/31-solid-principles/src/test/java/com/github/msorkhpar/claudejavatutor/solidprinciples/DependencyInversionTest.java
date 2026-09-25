package com.github.msorkhpar.claudejavatutor.solidprinciples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Dependency Inversion Principle Tests")
class DependencyInversionTest {

    @Nested
    @DisplayName("DIP Violation")
    class ViolationTest {

        @Test
        @DisplayName("Violation directly depends on concrete EmailSender")
        void testViolationDirectDependency() {
            var manager = new DependencyInversion.NotificationManagerViolation();
            manager.notifyUser("alice@example.com", "Hello");
            assertThat(manager.getSentNotifications())
                    .containsExactly("Email to alice@example.com: Hello");
        }
    }

    @Nested
    @DisplayName("EmailSender - MessageSender implementation")
    class EmailSenderTest {

        @Test
        @DisplayName("Should send email messages")
        void testSendEmail() {
            var sender = new DependencyInversion.EmailSender();
            sender.send("alice@test.com", "Hello");
            assertThat(sender.getSentMessages())
                    .containsExactly("EMAIL[alice@test.com]: Hello");
        }

        @Test
        @DisplayName("Should accumulate messages")
        void testAccumulateMessages() {
            var sender = new DependencyInversion.EmailSender();
            sender.send("a@test.com", "msg1");
            sender.send("b@test.com", "msg2");
            assertThat(sender.getSentMessages()).hasSize(2);
        }

        @Test
        @DisplayName("Should reject null recipient")
        void testNullRecipient() {
            var sender = new DependencyInversion.EmailSender();
            assertThatThrownBy(() -> sender.send(null, "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank recipient")
        void testBlankRecipient() {
            var sender = new DependencyInversion.EmailSender();
            assertThatThrownBy(() -> sender.send("  ", "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null message")
        void testNullMessage() {
            var sender = new DependencyInversion.EmailSender();
            assertThatThrownBy(() -> sender.send("a@test.com", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank message")
        void testBlankMessage() {
            var sender = new DependencyInversion.EmailSender();
            assertThatThrownBy(() -> sender.send("a@test.com", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("SmsSender - MessageSender implementation")
    class SmsSenderTest {

        @Test
        @DisplayName("Should send SMS messages")
        void testSendSms() {
            var sender = new DependencyInversion.SmsSender();
            sender.send("+1234567890", "Hello");
            assertThat(sender.getSentMessages())
                    .containsExactly("SMS[+1234567890]: Hello");
        }

        @Test
        @DisplayName("Should reject null recipient")
        void testNullRecipient() {
            var sender = new DependencyInversion.SmsSender();
            assertThatThrownBy(() -> sender.send(null, "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank message")
        void testBlankMessage() {
            var sender = new DependencyInversion.SmsSender();
            assertThatThrownBy(() -> sender.send("+123", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("NotificationManager - DIP Applied")
    class NotificationManagerTest {

        @Test
        @DisplayName("Should work with EmailSender")
        void testWithEmail() {
            var sender = new DependencyInversion.EmailSender();
            var manager = new DependencyInversion.NotificationManager(sender);
            manager.notifyUser("alice@test.com", "Hi");
            assertThat(manager.getSentNotifications())
                    .containsExactly("EMAIL[alice@test.com]: Hi");
        }

        @Test
        @DisplayName("Should work with SmsSender -- swappable via abstraction")
        void testWithSms() {
            var sender = new DependencyInversion.SmsSender();
            var manager = new DependencyInversion.NotificationManager(sender);
            manager.notifyUser("+123", "Hi");
            assertThat(manager.getSentNotifications())
                    .containsExactly("SMS[+123]: Hi");
        }

        @Test
        @DisplayName("Should reject null sender")
        void testNullSender() {
            assertThatThrownBy(() -> new DependencyInversion.NotificationManager(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ExecutionStrategy and JobScheduler - DIP in Concurrency")
    class JobSchedulerTest {

        private DependencyInversion.ExecutionStrategy strategy;

        @AfterEach
        void tearDown() {
            if (strategy != null) {
                strategy.shutdown();
            }
        }

        @Test
        @DisplayName("Should schedule jobs with FixedPoolStrategy")
        void testFixedPool() throws Exception {
            strategy = new DependencyInversion.FixedPoolStrategy(2);
            var scheduler = new DependencyInversion.JobScheduler(strategy);
            Future<Integer> future = scheduler.scheduleJob(() -> 42);
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo(42);
            assertThat(scheduler.getJobCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should schedule jobs with VirtualThreadStrategy")
        void testVirtualThreads() throws Exception {
            strategy = new DependencyInversion.VirtualThreadStrategy();
            var scheduler = new DependencyInversion.JobScheduler(strategy);
            Future<String> future = scheduler.scheduleJob(() -> "virtual");
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("virtual");
            assertThat(scheduler.getJobCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should track job count across multiple submissions")
        void testJobCount() throws Exception {
            strategy = new DependencyInversion.FixedPoolStrategy(2);
            var scheduler = new DependencyInversion.JobScheduler(strategy);
            scheduler.scheduleJob(() -> 1);
            scheduler.scheduleJob(() -> 2);
            scheduler.scheduleJob(() -> 3);
            assertThat(scheduler.getJobCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should reject null strategy")
        void testNullStrategy() {
            assertThatThrownBy(() -> new DependencyInversion.JobScheduler(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null job")
        void testNullJob() {
            strategy = new DependencyInversion.FixedPoolStrategy(1);
            var scheduler = new DependencyInversion.JobScheduler(strategy);
            assertThatThrownBy(() -> scheduler.scheduleJob(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("FixedPoolStrategy should reject non-positive pool size")
        void testInvalidPoolSize() {
            assertThatThrownBy(() -> new DependencyInversion.FixedPoolStrategy(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("FixedPoolStrategy should reject null task")
        void testNullTaskInStrategy() {
            strategy = new DependencyInversion.FixedPoolStrategy(1);
            assertThatThrownBy(() -> strategy.execute(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("BusinessService - DIP and Testability")
    class BusinessServiceTest {

        @Test
        @DisplayName("Should process and retrieve data")
        void testProcessAndRetrieve() {
            var repo = new DependencyInversion.InMemoryRepository();
            var logger = new DependencyInversion.InMemoryLogger();
            var service = new DependencyInversion.BusinessService(repo, logger);

            service.processData("k1", "v1");

            assertThat(service.getData("k1")).contains("v1");
            assertThat(logger.getLogEntries()).contains("Processed: k1", "Retrieving: k1");
        }

        @Test
        @DisplayName("Should list all data")
        void testGetAllData() {
            var repo = new DependencyInversion.InMemoryRepository();
            var logger = new DependencyInversion.InMemoryLogger();
            var service = new DependencyInversion.BusinessService(repo, logger);

            service.processData("a", "1");
            service.processData("b", "2");

            assertThat(service.getAllData()).containsExactlyInAnyOrder("1", "2");
            assertThat(logger.getLogEntries()).contains("Listing all data");
        }

        @Test
        @DisplayName("Should return empty for missing key")
        void testGetMissing() {
            var repo = new DependencyInversion.InMemoryRepository();
            var logger = new DependencyInversion.InMemoryLogger();
            var service = new DependencyInversion.BusinessService(repo, logger);

            assertThat(service.getData("nope")).isEmpty();
        }

        @Test
        @DisplayName("Should reject null key")
        void testNullKey() {
            var repo = new DependencyInversion.InMemoryRepository();
            var logger = new DependencyInversion.InMemoryLogger();
            var service = new DependencyInversion.BusinessService(repo, logger);

            assertThatThrownBy(() -> service.processData(null, "v"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank value")
        void testBlankValue() {
            var repo = new DependencyInversion.InMemoryRepository();
            var logger = new DependencyInversion.InMemoryLogger();
            var service = new DependencyInversion.BusinessService(repo, logger);

            assertThatThrownBy(() -> service.processData("k", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null repository")
        void testNullRepo() {
            assertThatThrownBy(() -> new DependencyInversion.BusinessService(null, new DependencyInversion.InMemoryLogger()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null logger")
        void testNullLogger() {
            assertThatThrownBy(() -> new DependencyInversion.BusinessService(new DependencyInversion.InMemoryRepository(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("InMemoryRepository")
    class InMemoryRepositoryTest {

        @Test
        @DisplayName("Should save and find by key")
        void testSaveAndFind() {
            var repo = new DependencyInversion.InMemoryRepository();
            repo.save("key", "value");
            assertThat(repo.findByKey("key")).contains("value");
        }

        @Test
        @DisplayName("Should return empty for missing key")
        void testFindMissing() {
            var repo = new DependencyInversion.InMemoryRepository();
            assertThat(repo.findByKey("nope")).isEmpty();
        }

        @Test
        @DisplayName("Should list all values")
        void testFindAll() {
            var repo = new DependencyInversion.InMemoryRepository();
            repo.save("a", "1");
            repo.save("b", "2");
            assertThat(repo.findAll()).containsExactlyInAnyOrder("1", "2");
        }

        @Test
        @DisplayName("Should reject null key")
        void testNullKey() {
            var repo = new DependencyInversion.InMemoryRepository();
            assertThatThrownBy(() -> repo.save(null, "v"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null value")
        void testNullValue() {
            var repo = new DependencyInversion.InMemoryRepository();
            assertThatThrownBy(() -> repo.save("k", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("InMemoryLogger")
    class InMemoryLoggerTest {

        @Test
        @DisplayName("Should log messages")
        void testLog() {
            var logger = new DependencyInversion.InMemoryLogger();
            logger.log("test message");
            assertThat(logger.getLogEntries()).containsExactly("test message");
        }

        @Test
        @DisplayName("Should reject null message")
        void testNullMessage() {
            var logger = new DependencyInversion.InMemoryLogger();
            assertThatThrownBy(() -> logger.log(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return unmodifiable log entries")
        void testUnmodifiable() {
            var logger = new DependencyInversion.InMemoryLogger();
            logger.log("test");
            assertThatThrownBy(() -> logger.getLogEntries().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
