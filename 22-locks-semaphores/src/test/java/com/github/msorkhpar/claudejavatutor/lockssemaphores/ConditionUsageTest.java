package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Tests for {@link ConditionUsage} (bounded blocking queue with Condition objects).
 * Covers: put/take, offer/poll with timeout, producer-consumer concurrency,
 * boundary conditions, null handling, capacity validation.
 */
@Timeout(30)
class ConditionUsageTest {

    private ConditionUsage<String> queue;

    @BeforeEach
    void setUp() {
        queue = new ConditionUsage<>(3);
    }

    // -----------------------------------------------------------------------
    // Basic put and take
    // -----------------------------------------------------------------------

    @Test
    void testPutAndTake() throws InterruptedException {
        queue.put("A");
        queue.put("B");

        assertThat(queue.take()).isEqualTo("A");
        assertThat(queue.take()).isEqualTo("B");
    }

    @Test
    void testFIFOOrdering() throws InterruptedException {
        queue.put("first");
        queue.put("second");
        queue.put("third");

        assertThat(queue.take()).isEqualTo("first");
        assertThat(queue.take()).isEqualTo("second");
        assertThat(queue.take()).isEqualTo("third");
    }

    // -----------------------------------------------------------------------
    // Size, isEmpty, isFull
    // -----------------------------------------------------------------------

    @Test
    void testSizeAfterPutAndTake() throws InterruptedException {
        assertThat(queue.size()).isZero();
        assertThat(queue.isEmpty()).isTrue();
        assertThat(queue.isFull()).isFalse();

        queue.put("A");
        assertThat(queue.size()).isEqualTo(1);
        assertThat(queue.isEmpty()).isFalse();

        queue.put("B");
        queue.put("C");
        assertThat(queue.size()).isEqualTo(3);
        assertThat(queue.isFull()).isTrue();

        queue.take();
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.isFull()).isFalse();
    }

    @Test
    void testGetCapacity() {
        assertThat(queue.getCapacity()).isEqualTo(3);
    }

    // -----------------------------------------------------------------------
    // Blocking behavior: put blocks when full, take blocks when empty
    // -----------------------------------------------------------------------

    @Test
    void testPutBlocksWhenFull() throws InterruptedException {
        queue.put("A");
        queue.put("B");
        queue.put("C");
        // Queue is now full

        AtomicBoolean putCompleted = new AtomicBoolean(false);
        Thread producer = Thread.ofVirtual().start(() -> {
            try {
                queue.put("D"); // should block
                putCompleted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Producer should be blocked
        Thread.sleep(200);
        assertThat(putCompleted.get()).isFalse();

        // Take one item to unblock producer
        queue.take();
        await().atMost(5, TimeUnit.SECONDS).untilTrue(putCompleted);

        producer.join(5000);
    }

    @Test
    void testTakeBlocksWhenEmpty() throws InterruptedException {
        AtomicReference<String> result = new AtomicReference<>();
        Thread consumer = Thread.ofVirtual().start(() -> {
            try {
                result.set(queue.take()); // should block
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer should be blocked
        Thread.sleep(200);
        assertThat(result.get()).isNull();

        // Put an item to unblock consumer
        queue.put("hello");
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(result.get()).isEqualTo("hello"));

        consumer.join(5000);
    }

    // -----------------------------------------------------------------------
    // Timed offer and poll
    // -----------------------------------------------------------------------

    @Test
    void testOfferWithTimeout() throws InterruptedException {
        boolean result = queue.offer("A", 100, TimeUnit.MILLISECONDS);
        assertThat(result).isTrue();
        assertThat(queue.size()).isEqualTo(1);
    }

    @Test
    void testOfferTimesOutWhenFull() throws InterruptedException {
        queue.put("A");
        queue.put("B");
        queue.put("C");

        boolean result = queue.offer("D", 100, TimeUnit.MILLISECONDS);
        assertThat(result).isFalse();
        assertThat(queue.size()).isEqualTo(3);
    }

    @Test
    void testPollWithTimeout() throws InterruptedException {
        queue.put("X");
        String result = queue.poll(100, TimeUnit.MILLISECONDS);
        assertThat(result).isEqualTo("X");
    }

    @Test
    void testPollTimesOutWhenEmpty() throws InterruptedException {
        String result = queue.poll(100, TimeUnit.MILLISECONDS);
        assertThat(result).isNull();
    }

    // -----------------------------------------------------------------------
    // Producer-consumer concurrency
    // -----------------------------------------------------------------------

    @Test
    void testMultipleProducersAndConsumers() throws InterruptedException {
        int itemCount = 100;
        List<String> produced = Collections.synchronizedList(new ArrayList<>());
        List<String> consumed = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(itemCount);

        // 3 producers
        for (int p = 0; p < 3; p++) {
            final int producerId = p;
            Thread.ofVirtual().start(() -> {
                for (int i = 0; i < itemCount / 3 + (producerId < itemCount % 3 ? 1 : 0); i++) {
                    try {
                        String item = "P" + producerId + "-" + i;
                        queue.put(item);
                        produced.add(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 3 consumers
        for (int c = 0; c < 3; c++) {
            Thread.ofVirtual().start(() -> {
                while (true) {
                    try {
                        String item = queue.poll(2, TimeUnit.SECONDS);
                        if (item == null) break;
                        consumed.add(item);
                        done.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        done.await(30, TimeUnit.SECONDS);

        assertThat(consumed).hasSize(itemCount);
        assertThat(consumed).containsExactlyInAnyOrderElementsOf(produced);
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    void testCapacityOfOne() throws InterruptedException {
        ConditionUsage<Integer> tinyQueue = new ConditionUsage<>(1);
        tinyQueue.put(42);
        assertThat(tinyQueue.isFull()).isTrue();
        assertThat(tinyQueue.take()).isEqualTo(42);
        assertThat(tinyQueue.isEmpty()).isTrue();
    }

    @Test
    void testNullItemThrowsNullPointerException() {
        assertThatThrownBy(() -> queue.put(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Null");
    }

    @Test
    void testNullItemInOfferThrowsNullPointerException() {
        assertThatThrownBy(() -> queue.offer(null, 100, TimeUnit.MILLISECONDS))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Null");
    }

    @Test
    void testInvalidCapacityThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new ConditionUsage<>(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConditionUsage<>(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // Interruption during blocking
    // -----------------------------------------------------------------------

    @Test
    void testTakeInterruptedWhileWaiting() throws InterruptedException {
        AtomicReference<Exception> caught = new AtomicReference<>();
        Thread consumer = Thread.ofPlatform().start(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                caught.set(e);
            }
        });

        Thread.sleep(200);
        consumer.interrupt();
        consumer.join(5000);

        assertThat(caught.get()).isInstanceOf(InterruptedException.class);
    }

    @Test
    void testPutInterruptedWhileWaiting() throws InterruptedException {
        queue.put("A");
        queue.put("B");
        queue.put("C"); // full

        AtomicReference<Exception> caught = new AtomicReference<>();
        Thread producer = Thread.ofPlatform().start(() -> {
            try {
                queue.put("D");
            } catch (InterruptedException e) {
                caught.set(e);
            }
        });

        Thread.sleep(200);
        producer.interrupt();
        producer.join(5000);

        assertThat(caught.get()).isInstanceOf(InterruptedException.class);
    }

    // -----------------------------------------------------------------------
    // Signal methods
    // -----------------------------------------------------------------------

    @Test
    void testSignalAllProducersAndConsumers() {
        // These should not throw even when no threads are waiting
        queue.signalAllProducers();
        queue.signalAllConsumers();
    }
}
