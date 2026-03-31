package com.github.msorkhpar.claudejavatutor.kissprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KISS Benefits for Concurrency Tests")
class KissBenefitsTest {

    @Nested
    @DisplayName("Easy-to-Debug Simple Design")
    class EasyToDebugTest {

        @Test
        @DisplayName("Simple event logger should log events in order")
        void testSimpleEventLoggerOrder() {
            var logger = new KissBenefits.SimpleEventLogger();

            logger.log("event1");
            logger.log("event2");
            logger.log("event3");

            assertThat(logger.getEvents()).containsExactly("event1", "event2", "event3");
        }

        @Test
        @DisplayName("Simple event logger should start empty")
        void testSimpleEventLoggerEmpty() {
            var logger = new KissBenefits.SimpleEventLogger();

            assertThat(logger.getEvents()).isEmpty();
        }

        @Test
        @DisplayName("Simple event logger should be thread-safe")
        void testSimpleEventLoggerThreadSafe() throws InterruptedException {
            var logger = new KissBenefits.SimpleEventLogger();
            int threads = 10;
            int eventsPerThread = 100;
            var latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                Thread.ofVirtual().start(() -> {
                    for (int j = 0; j < eventsPerThread; j++) {
                        logger.log("thread" + threadId + "-event" + j);
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(logger.getEvents()).hasSize(threads * eventsPerThread);
        }

        @Test
        @DisplayName("Simple event logger should support clear")
        void testSimpleEventLoggerClear() {
            var logger = new KissBenefits.SimpleEventLogger();

            logger.log("event1");
            logger.log("event2");
            logger.clear();

            assertThat(logger.getEvents()).isEmpty();
        }

        @Test
        @DisplayName("Simple event logger getEvents should return immutable snapshot")
        void testSimpleEventLoggerImmutableSnapshot() {
            var logger = new KissBenefits.SimpleEventLogger();
            logger.log("event1");

            List<String> snapshot = logger.getEvents();

            assertThatThrownBy(() -> snapshot.add("should fail"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Reduced Concurrency Bug Risk")
    class ReducedBugRiskTest {

        @Test
        @DisplayName("Simple state machine should transition correctly")
        void testSimpleStateMachineTransition() {
            var sm = new KissBenefits.SimpleStateMachine();

            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.IDLE);

            assertThat(sm.start()).isTrue();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.RUNNING);

            assertThat(sm.complete()).isTrue();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.COMPLETED);
        }

        @Test
        @DisplayName("Simple state machine should reject invalid transitions")
        void testSimpleStateMachineInvalidTransition() {
            var sm = new KissBenefits.SimpleStateMachine();

            // Cannot complete from IDLE directly
            assertThat(sm.complete()).isFalse();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.IDLE);
        }

        @Test
        @DisplayName("Simple state machine should not start twice")
        void testSimpleStateMachineDoubleStart() {
            var sm = new KissBenefits.SimpleStateMachine();

            assertThat(sm.start()).isTrue();
            assertThat(sm.start()).isFalse();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.RUNNING);
        }

        @Test
        @DisplayName("Simple state machine should handle fail transition")
        void testSimpleStateMachineFailTransition() {
            var sm = new KissBenefits.SimpleStateMachine();

            sm.start();
            assertThat(sm.fail()).isTrue();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.FAILED);
        }

        @Test
        @DisplayName("Simple state machine should reset from any terminal state")
        void testSimpleStateMachineReset() {
            var sm = new KissBenefits.SimpleStateMachine();

            sm.start();
            sm.complete();
            assertThat(sm.reset()).isTrue();
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.IDLE);
        }

        @Test
        @DisplayName("Simple state machine should be thread-safe for concurrent transitions")
        void testSimpleStateMachineConcurrentTransitions() throws InterruptedException {
            var sm = new KissBenefits.SimpleStateMachine();
            int threads = 10;
            var successCount = new java.util.concurrent.atomic.AtomicInteger(0);
            var latch = new CountDownLatch(threads);

            // Multiple threads try to start simultaneously - only one should succeed
            for (int i = 0; i < threads; i++) {
                Thread.ofVirtual().start(() -> {
                    if (sm.start()) {
                        successCount.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(sm.getState()).isEqualTo(KissBenefits.SimpleStateMachine.State.RUNNING);
        }
    }

    @Nested
    @DisplayName("Simple Task Pipeline")
    class SimpleTaskPipelineTest {

        @Test
        @DisplayName("Pipeline should apply transformations in order")
        void testPipelineTransformationsInOrder() {
            var pipeline = new KissBenefits.SimpleTaskPipeline<String>();

            pipeline.addStep(String::toUpperCase);
            pipeline.addStep(s -> s + "!");

            String result = pipeline.execute("hello");

            assertThat(result).isEqualTo("HELLO!");
        }

        @Test
        @DisplayName("Pipeline with no steps should return input unchanged")
        void testPipelineNoSteps() {
            var pipeline = new KissBenefits.SimpleTaskPipeline<String>();

            String result = pipeline.execute("hello");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Pipeline should handle single step")
        void testPipelineSingleStep() {
            var pipeline = new KissBenefits.SimpleTaskPipeline<Integer>();

            pipeline.addStep(n -> n * 2);

            assertThat(pipeline.execute(5)).isEqualTo(10);
        }

        @Test
        @DisplayName("Pipeline should handle null input gracefully")
        void testPipelineNullInput() {
            var pipeline = new KissBenefits.SimpleTaskPipeline<String>();
            pipeline.addStep(s -> s == null ? "default" : s);

            assertThat(pipeline.execute(null)).isEqualTo("default");
        }

        @Test
        @DisplayName("Pipeline should chain multiple steps")
        void testPipelineMultipleSteps() {
            var pipeline = new KissBenefits.SimpleTaskPipeline<Integer>();

            pipeline.addStep(n -> n + 1);
            pipeline.addStep(n -> n * 2);
            pipeline.addStep(n -> n - 3);

            // (5 + 1) * 2 - 3 = 9
            assertThat(pipeline.execute(5)).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("Simple Resource Manager")
    class SimpleResourceManagerTest {

        @Test
        @DisplayName("Resource manager should acquire and release resources")
        void testAcquireAndRelease() throws InterruptedException {
            var manager = new KissBenefits.SimpleResourceManager(3);

            assertThat(manager.tryAcquire()).isTrue();
            assertThat(manager.availableResources()).isEqualTo(2);

            manager.release();
            assertThat(manager.availableResources()).isEqualTo(3);
        }

        @Test
        @DisplayName("Resource manager should respect capacity limit")
        void testCapacityLimit() throws InterruptedException {
            var manager = new KissBenefits.SimpleResourceManager(2);

            assertThat(manager.tryAcquire()).isTrue();
            assertThat(manager.tryAcquire()).isTrue();
            // Third acquire should fail (non-blocking)
            assertThat(manager.tryAcquire()).isFalse();
        }

        @Test
        @DisplayName("Resource manager should work across threads")
        void testResourceManagerCrossThread() throws InterruptedException {
            var manager = new KissBenefits.SimpleResourceManager(1);
            var acquired = new AtomicBoolean(false);
            var latch = new CountDownLatch(1);

            // Acquire in main thread
            manager.tryAcquire();

            // Try to acquire in another thread
            Thread.ofVirtual().start(() -> {
                try {
                    acquired.set(manager.tryAcquire());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            assertThat(acquired.get()).isFalse();

            // Release and try again
            manager.release();
            var latch2 = new CountDownLatch(1);
            Thread.ofVirtual().start(() -> {
                try {
                    acquired.set(manager.tryAcquire());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch2.countDown();
            });

            latch2.await(5, TimeUnit.SECONDS);
            assertThat(acquired.get()).isTrue();
        }

        @Test
        @DisplayName("Resource manager should throw on invalid capacity")
        void testInvalidCapacity() {
            assertThatThrownBy(() -> new KissBenefits.SimpleResourceManager(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Notification System")
    class SimpleNotificationTest {

        @Test
        @DisplayName("Simple notification should notify all listeners")
        void testNotifyAllListeners() {
            var notifier = new KissBenefits.SimpleNotifier<String>();
            var received = new CopyOnWriteArrayList<String>();

            notifier.addListener(received::add);
            notifier.addListener(msg -> received.add(msg.toUpperCase()));

            notifier.notify("hello");

            assertThat(received).containsExactly("hello", "HELLO");
        }

        @Test
        @DisplayName("Simple notification should handle no listeners")
        void testNotifyNoListeners() {
            var notifier = new KissBenefits.SimpleNotifier<String>();

            // Should not throw
            assertThatCode(() -> notifier.notify("hello")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Simple notification should handle concurrent notifications")
        void testConcurrentNotifications() throws InterruptedException {
            var notifier = new KissBenefits.SimpleNotifier<Integer>();
            var received = new CopyOnWriteArrayList<Integer>();
            notifier.addListener(received::add);

            int threads = 10;
            var latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                final int value = i;
                Thread.ofVirtual().start(() -> {
                    notifier.notify(value);
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            assertThat(received).hasSize(threads);
        }
    }
}
