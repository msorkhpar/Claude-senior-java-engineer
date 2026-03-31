package com.github.msorkhpar.claudejavatutor.dryprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DRY Benefits for Concurrency Tests")
class DryBenefitsTest {

    @Nested
    @DisplayName("Violation: Duplicated Read-Write Locking")
    class ViolationUserRepositoryTest {

        @Test
        @DisplayName("Should add and retrieve user")
        void testAddAndGetUser() {
            var repo = new DryBenefits.ViolationUserRepository();
            repo.addUser("1", "Alice");
            assertThat(repo.getUser("1")).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should return null for missing user")
        void testGetMissingUser() {
            var repo = new DryBenefits.ViolationUserRepository();
            assertThat(repo.getUser("missing")).isNull();
        }

        @Test
        @DisplayName("Should get all users")
        void testGetAllUsers() {
            var repo = new DryBenefits.ViolationUserRepository();
            repo.addUser("1", "Alice");
            repo.addUser("2", "Bob");
            assertThat(repo.getAllUsers()).containsExactlyInAnyOrder("Alice", "Bob");
        }
    }

    @Nested
    @DisplayName("ReadWriteLockedResource - Reusable RW Lock Wrapper")
    class ReadWriteLockedResourceTest {

        @Test
        @DisplayName("Should read from resource")
        void testRead() {
            var locked = new DryBenefits.ReadWriteLockedResource<>(new ArrayList<>(List.of("a", "b")));
            int size = locked.read(List::size);
            assertThat(size).isEqualTo(2);
        }

        @Test
        @DisplayName("Should write to resource")
        void testWrite() {
            var locked = new DryBenefits.ReadWriteLockedResource<>(new ArrayList<String>());
            int newSize = locked.write(list -> {
                list.add("item");
                return list.size();
            });
            assertThat(newSize).isEqualTo(1);
        }

        @Test
        @DisplayName("Should write void to resource")
        void testWriteVoid() {
            var locked = new DryBenefits.ReadWriteLockedResource<>(new ArrayList<String>());
            locked.writeVoid(list -> list.add("item"));
            int size = locked.read(List::size);
            assertThat(size).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reject null resource")
        void testNullResource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DryBenefits.ReadWriteLockedResource<>(null));
        }

        @Test
        @DisplayName("Should reject null read action")
        void testNullReadAction() {
            var locked = new DryBenefits.ReadWriteLockedResource<>(new ArrayList<>());
            assertThatNullPointerException()
                    .isThrownBy(() -> locked.read(null));
        }

        @Test
        @DisplayName("Should reject null write action")
        void testNullWriteAction() {
            var locked = new DryBenefits.ReadWriteLockedResource<>(new ArrayList<>());
            assertThatNullPointerException()
                    .isThrownBy(() -> locked.write(null));
        }

        @Test
        @DisplayName("Should handle concurrent reads safely")
        void testConcurrentReads() throws InterruptedException, ExecutionException {
            var data = new ArrayList<>(List.of("a", "b", "c"));
            var locked = new DryBenefits.ReadWriteLockedResource<>(data);
            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> locked.read(List::size)));
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            for (Future<Integer> f : futures) {
                assertThat(f.get()).isEqualTo(3);
            }
        }
    }

    @Nested
    @DisplayName("DryUserRepository - Using ReadWriteLockedResource")
    class DryUserRepositoryTest {

        @Test
        @DisplayName("Should add and retrieve user")
        void testAddAndGetUser() {
            var repo = new DryBenefits.DryUserRepository();
            repo.addUser("1", "Alice");
            assertThat(repo.getUser("1")).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should return null for missing user")
        void testGetMissingUser() {
            var repo = new DryBenefits.DryUserRepository();
            assertThat(repo.getUser("missing")).isNull();
        }

        @Test
        @DisplayName("Should get all users")
        void testGetAllUsers() {
            var repo = new DryBenefits.DryUserRepository();
            repo.addUser("1", "Alice");
            repo.addUser("2", "Bob");
            assertThat(repo.getAllUsers()).containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        @DisplayName("Should report correct size")
        void testSize() {
            var repo = new DryBenefits.DryUserRepository();
            assertThat(repo.size()).isEqualTo(0);
            repo.addUser("1", "Alice");
            assertThat(repo.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle concurrent add and read safely")
        void testConcurrentAddAndRead() throws InterruptedException {
            var repo = new DryBenefits.DryUserRepository();
            int threads = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                final int idx = i;
                executor.submit(() -> repo.addUser(String.valueOf(idx), "User" + idx));
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(repo.size()).isEqualTo(threads);
        }
    }

    @Nested
    @DisplayName("DryProductRepository - Reusing ReadWriteLockedResource")
    class DryProductRepositoryTest {

        @Test
        @DisplayName("Should add and get product price")
        void testAddAndGetProduct() {
            var repo = new DryBenefits.DryProductRepository();
            repo.addProduct("Widget", 9.99);
            assertThat(repo.getPrice("Widget")).isEqualTo(9.99);
        }

        @Test
        @DisplayName("Should return null for missing product")
        void testGetMissingProduct() {
            var repo = new DryBenefits.DryProductRepository();
            assertThat(repo.getPrice("missing")).isNull();
        }

        @Test
        @DisplayName("Should get all product names")
        void testGetAllProductNames() {
            var repo = new DryBenefits.DryProductRepository();
            repo.addProduct("A", 1.0);
            repo.addProduct("B", 2.0);
            assertThat(repo.getAllProductNames()).containsExactlyInAnyOrder("A", "B");
        }
    }

    @Nested
    @DisplayName("ConsistentCounter - Unified Thread-Safe Counter")
    class ConsistentCounterTest {

        @Test
        @DisplayName("Should increment and return new value")
        void testIncrement() {
            var counter = new DryBenefits.ConsistentCounter();
            assertThat(counter.increment()).isEqualTo(1);
            assertThat(counter.increment()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should decrement and return new value")
        void testDecrement() {
            var counter = new DryBenefits.ConsistentCounter();
            counter.increment();
            counter.increment();
            assertThat(counter.decrement()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get current value")
        void testGet() {
            var counter = new DryBenefits.ConsistentCounter();
            assertThat(counter.get()).isEqualTo(0);
            counter.increment();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reset to zero")
        void testReset() {
            var counter = new DryBenefits.ConsistentCounter();
            counter.increment();
            counter.increment();
            counter.reset();
            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle concurrent increments safely")
        void testConcurrentIncrements() throws InterruptedException {
            var counter = new DryBenefits.ConsistentCounter();
            int threads = 10;
            int incrementsPerThread = 1000;
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(counter.get()).isEqualTo(threads * incrementsPerThread);
        }
    }

    @Nested
    @DisplayName("SimpleEventBus - Consistent Publish-Subscribe")
    class SimpleEventBusTest {

        @Test
        @DisplayName("Should notify subscriber on publish")
        void testPublishNotifiesSubscriber() {
            var bus = new DryBenefits.SimpleEventBus();
            AtomicInteger received = new AtomicInteger(0);

            bus.subscribe("click", event -> received.incrementAndGet());
            bus.publish("click", "data");

            assertThat(received.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should notify multiple subscribers")
        void testMultipleSubscribers() {
            var bus = new DryBenefits.SimpleEventBus();
            AtomicInteger counter = new AtomicInteger(0);

            bus.subscribe("event", e -> counter.incrementAndGet());
            bus.subscribe("event", e -> counter.incrementAndGet());
            bus.publish("event", null);

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should not notify unrelated subscribers")
        void testUnrelatedSubscribersNotNotified() {
            var bus = new DryBenefits.SimpleEventBus();
            AtomicInteger counter = new AtomicInteger(0);

            bus.subscribe("typeA", e -> counter.incrementAndGet());
            bus.publish("typeB", "data");

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle publish with no subscribers gracefully")
        void testPublishNoSubscribers() {
            var bus = new DryBenefits.SimpleEventBus();
            assertThatCode(() -> bus.publish("event", "data")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should report listener count")
        void testListenerCount() {
            var bus = new DryBenefits.SimpleEventBus();
            assertThat(bus.listenerCount("event")).isEqualTo(0);
            bus.subscribe("event", e -> {});
            bus.subscribe("event", e -> {});
            assertThat(bus.listenerCount("event")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should reject null event type in subscribe")
        void testNullEventTypeSubscribe() {
            var bus = new DryBenefits.SimpleEventBus();
            assertThatNullPointerException()
                    .isThrownBy(() -> bus.subscribe(null, e -> {}));
        }

        @Test
        @DisplayName("Should reject null listener in subscribe")
        void testNullListenerSubscribe() {
            var bus = new DryBenefits.SimpleEventBus();
            assertThatNullPointerException()
                    .isThrownBy(() -> bus.subscribe("event", null));
        }

        @Test
        @DisplayName("Should reject null event type in publish")
        void testNullEventTypePublish() {
            var bus = new DryBenefits.SimpleEventBus();
            assertThatNullPointerException()
                    .isThrownBy(() -> bus.publish(null, "data"));
        }
    }
}
