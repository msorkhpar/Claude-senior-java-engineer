package com.github.msorkhpar.claudejavatutor.lockssemaphores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests for {@link ReadWriteLockUsage}.
 * Covers: concurrent reads, exclusive writes, lock downgrading,
 * putIfAbsent, diagnostics, fairness.
 */
@Timeout(30)
class ReadWriteLockUsageTest {

    private ReadWriteLockUsage<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ReadWriteLockUsage<>();
    }

    // -----------------------------------------------------------------------
    // Basic CRUD
    // -----------------------------------------------------------------------

    @Test
    void testPutAndGet() {
        assertThat(cache.put("key1", "value1")).isNull();
        assertThat(cache.get("key1")).isEqualTo("value1");
    }

    @Test
    void testPutReturnsOldValue() {
        cache.put("key", "old");
        assertThat(cache.put("key", "new")).isEqualTo("old");
        assertThat(cache.get("key")).isEqualTo("new");
    }

    @Test
    void testGetNonExistentKey() {
        assertThat(cache.get("missing")).isNull();
    }

    @Test
    void testRemove() {
        cache.put("key", "value");
        assertThat(cache.remove("key")).isEqualTo("value");
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void testRemoveNonExistentKey() {
        assertThat(cache.remove("missing")).isNull();
    }

    @Test
    void testContainsKey() {
        assertThat(cache.containsKey("key")).isFalse();
        cache.put("key", "value");
        assertThat(cache.containsKey("key")).isTrue();
    }

    @Test
    void testSize() {
        assertThat(cache.size()).isZero();
        cache.put("a", "1");
        cache.put("b", "2");
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void testClear() {
        cache.put("a", "1");
        cache.put("b", "2");
        cache.clear();
        assertThat(cache.size()).isZero();
        assertThat(cache.get("a")).isNull();
    }

    // -----------------------------------------------------------------------
    // putIfAbsent
    // -----------------------------------------------------------------------

    @Test
    void testPutIfAbsentWhenAbsent() {
        String result = cache.putIfAbsent("key", "value");
        assertThat(result).isEqualTo("value");
        assertThat(cache.get("key")).isEqualTo("value");
    }

    @Test
    void testPutIfAbsentWhenPresent() {
        cache.put("key", "existing");
        String result = cache.putIfAbsent("key", "new");
        assertThat(result).isEqualTo("existing");
        assertThat(cache.get("key")).isEqualTo("existing");
    }

    // -----------------------------------------------------------------------
    // Lock downgrading
    // -----------------------------------------------------------------------

    @Test
    void testPutAndReadWithDowngrade() {
        String result = cache.putAndReadWithDowngrade("key", "downgraded-value");
        assertThat(result).isEqualTo("downgraded-value");
        assertThat(cache.get("key")).isEqualTo("downgraded-value");
    }

    // -----------------------------------------------------------------------
    // Concurrent reads do not block each other
    // -----------------------------------------------------------------------

    @Test
    void testConcurrentReads() throws Exception {
        cache.put("shared", "data");

        int readerCount = 10;
        // The barrier can only trip once all readers hold the read lock at the same time,
        // which proves that readers do not block each other.
        CyclicBarrier allHolding = new CyclicBarrier(readerCount + 1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(readerCount);
        List<String> seen = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < readerCount; i++) {
            Thread.ofVirtual().start(() -> {
                cache.getLock().readLock().lock();
                try {
                    seen.add(cache.get("shared")); // reentrant read under the held read lock
                    allHolding.await(5, TimeUnit.SECONDS);
                    release.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    cache.getLock().readLock().unlock();
                    done.countDown();
                }
            });
        }

        allHolding.await(5, TimeUnit.SECONDS); // throws TimeoutException if readers blocked each other
        assertThat(cache.getReadLockCount()).isEqualTo(readerCount);
        assertThat(cache.get("shared")).isEqualTo("data"); // another reader still gets in
        assertThat(cache.isWriteLocked()).isFalse();

        release.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(seen).hasSize(readerCount).containsOnly("data");
        assertThat(cache.getReadLockCount()).isZero();
    }

    // -----------------------------------------------------------------------
    // Writer excludes readers and other writers
    // -----------------------------------------------------------------------

    @Test
    void testConcurrentWritesAreExclusive() throws InterruptedException {
        int writerCount = 5;
        int writesPerWriter = 100;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(writerCount);

        for (int w = 0; w < writerCount; w++) {
            final int writerId = w;
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    for (int i = 0; i < writesPerWriter; i++) {
                        cache.put("key-" + writerId + "-" + i, "val-" + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(10, TimeUnit.SECONDS);

        assertThat(cache.size()).isEqualTo(writerCount * writesPerWriter);
    }

    @Test
    void testMixedReadWriteConcurrency() throws InterruptedException {
        int operations = 500;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(4);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        // 2 writers
        for (int w = 0; w < 2; w++) {
            final int writerId = w;
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    for (int i = 0; i < operations; i++) {
                        cache.put("w" + writerId + "-" + i, "v" + i);
                    }
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        // 2 readers
        for (int r = 0; r < 2; r++) {
            Thread.ofVirtual().start(() -> {
                try {
                    start.await();
                    for (int i = 0; i < operations; i++) {
                        cache.size(); // read operation
                        cache.containsKey("w0-" + i); // read operation
                    }
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(15, TimeUnit.SECONDS);

        assertThat(errors).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Diagnostics
    // -----------------------------------------------------------------------

    @Test
    void testDiagnosticMethodsDefaultState() {
        assertThat(cache.getReadLockCount()).isZero();
        assertThat(cache.isWriteLocked()).isFalse();
    }

    @Test
    void testFairnessFlag() {
        assertThat(cache.isFair()).isFalse();

        ReadWriteLockUsage<String, String> fairCache = new ReadWriteLockUsage<>(true);
        assertThat(fairCache.isFair()).isTrue();
    }
}
