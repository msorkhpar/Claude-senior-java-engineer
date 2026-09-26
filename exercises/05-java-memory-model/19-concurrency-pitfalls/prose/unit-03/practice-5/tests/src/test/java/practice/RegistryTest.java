package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RegistryTest {

    @Test
    @Order(1)
    void nothingIsCreatedBeforeFirstUse() {
        assertThat(Registry.instancesCreated()).as("no getInstance() call has been made yet").isZero();
    }

    @Test
    @Order(2)
    void everyThreadGetsTheOneInstance() throws InterruptedException {
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        AtomicReferenceArray<Registry> seen = new AtomicReferenceArray<>(threads);
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int slot = i;
            Thread t = new Thread(() -> {
                try {
                    if (start.await(20, TimeUnit.SECONDS)) {
                        seen.set(slot, Registry.getInstance());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.setDaemon(true);
            t.start();
            workers.add(t);
        }
        start.countDown();
        for (Thread t : workers) {
            t.join(20_000);
            assertThat(t.isAlive()).isFalse();
        }
        Registry one = Registry.getInstance();
        for (int i = 0; i < threads; i++) {
            assertThat(seen.get(i)).isSameAs(one);
        }
        assertThat(Registry.instancesCreated()).isEqualTo(1);
    }
}
