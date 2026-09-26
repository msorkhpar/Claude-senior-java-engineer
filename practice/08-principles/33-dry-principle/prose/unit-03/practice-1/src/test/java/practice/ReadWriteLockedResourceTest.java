package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class ReadWriteLockedResourceTest {

    @Test
    void bothRepositoriesShareTheWrapper() throws Exception {
        ReadWriteLockedResource.UserRepository users = new ReadWriteLockedResource.UserRepository();
        assertThat(users.getAllUsers()).isEmpty();
        users.addUser("u-1", "Ann");
        users.addUser("u-2", "Bob");
        assertThat(users.getUser("u-1")).isEqualTo("Ann");
        assertThat(users.getUser("u-9")).isNull();
        assertThat(users.getAllUsers()).containsExactlyInAnyOrder("Ann", "Bob");
        ReadWriteLockedResource.ProductRepository products = new ReadWriteLockedResource.ProductRepository();
        products.addProduct("pen", 1.5);
        assertThat(products.getPrice("pen")).isEqualTo(1.5);
        ReadWriteLockedResource<List<String>> list = new ReadWriteLockedResource<>(new ArrayList<>());
        list.writeVoid(l -> l.add("x"));
        int size = list.read(List::size);
        assertThat(size).isEqualTo(1);
    }

    @Test
    void readersDoNotBlockEachOther() throws Exception {
        ReadWriteLockedResource<Map<String, String>> shared = new ReadWriteLockedResource<>(new HashMap<>());
        CountDownLatch bothInside = new CountDownLatch(2);
        Callable<Boolean> reader = () -> shared.read(map -> {
            bothInside.countDown();
            try {
                return bothInside.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = pool.submit(reader);
            Future<Boolean> second = pool.submit(reader);
            assertThat(first.get()).as("first reader saw the second one inside at the same time").isTrue();
            assertThat(second.get()).as("second reader saw the first one inside at the same time").isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aWriterHoldsReadersBack() throws Exception {
        ReadWriteLockedResource<Map<String, String>> shared = new ReadWriteLockedResource<>(new HashMap<>());
        CountDownLatch writerInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread writer = new Thread(() -> shared.writeVoid(map -> {
            map.put("k", "v");
            writerInside.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }));
        writer.start();
        assertThat(writerInside.await(30, TimeUnit.SECONDS)).as("the writer got inside writeVoid").isTrue();
        AtomicReference<String> seen = new AtomicReference<>();
        Thread reader = new Thread(() -> seen.set(shared.read(map -> map.get("k"))));
        reader.start();
        Thread.State state;
        while ((state = reader.getState()) != Thread.State.WAITING && state != Thread.State.TERMINATED) {
            Thread.onSpinWait();
        }
        release.countDown();
        writer.join();
        reader.join();
        assertThat(state).as("the reader while the writer was inside").isEqualTo(Thread.State.WAITING);
        assertThat(seen.get()).isEqualTo("v");
    }

    @Test
    void nullsAreRefusedBeforeAnyLocking() throws Exception {
        assertThatThrownBy(() -> new ReadWriteLockedResource<Map<String, String>>(null)).isInstanceOf(NullPointerException.class);
        ReadWriteLockedResource<Map<String, String>> shared = new ReadWriteLockedResource<>(new HashMap<>());
        assertThatThrownBy(() -> shared.read(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> shared.writeVoid(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aNullActionIsRefusedWithoutWaiting() throws Exception {
        ReadWriteLockedResource<java.util.List<String>> shared = new ReadWriteLockedResource<>(new java.util.ArrayList<>());
        java.util.concurrent.CountDownLatch inside = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread writer = new Thread(() -> shared.writeVoid(list -> {
            inside.countDown();
            try {
                release.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        writer.setDaemon(true);
        writer.start();
        assertThat(inside.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        try {
            org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                    assertThatThrownBy(() -> shared.read(null)).isInstanceOf(NullPointerException.class));
        } finally {
            release.countDown();
            writer.join(5_000);
        }
    }

    @Test
    void aThrowingActionStillReleasesTheLock() throws Exception {
        ReadWriteLockedResource<java.util.List<String>> shared = new ReadWriteLockedResource<>(new java.util.ArrayList<>());
        IllegalStateException boom = new IllegalStateException("boom");
        assertThatThrownBy(() -> shared.read(list -> {
            throw boom;
        })).isSameAs(boom);
        Thread writer = new Thread(() -> shared.writeVoid(list -> list.add("after")));
        writer.setDaemon(true);
        writer.start();
        writer.join(3_000);
        assertThat(writer.isAlive()).as("the write was not held back by a leaked read lock").isFalse();
        int size = shared.read(list -> list.size());
        assertThat(size).isEqualTo(1);
    }
}
