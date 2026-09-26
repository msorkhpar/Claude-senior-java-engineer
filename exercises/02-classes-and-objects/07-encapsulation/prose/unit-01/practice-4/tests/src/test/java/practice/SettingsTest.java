package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

// The instance is shared by the whole run, so the first calls are made by the concurrent case.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SettingsTest {

    @Order(1)
    @Test
    void concurrentFirstCallsCreateOneInstance() throws Exception {
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            Set<Settings> seen = ConcurrentHashMap.newKeySet();
            List<Future<?>> calls = IntStream.range(0, threads)
                    .<Future<?>>mapToObj(i -> pool.submit(() -> {
                        start.await();
                        seen.add(Settings.getInstance());
                        return null;
                    }))
                    .toList();
            start.countDown();
            for (Future<?> call : calls) {
                call.get(30, TimeUnit.SECONDS);
            }
            assertThat(seen).hasSize(1);
            assertThat(Settings.created()).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Order(2)
    @Test
    void everyCallReturnsTheOneInstance() {
        Settings first = Settings.getInstance();
        assertThat(Settings.getInstance()).isSameAs(first);
        assertThat(first.get("mode")).isEqualTo("prod");
        assertThat(first.get("missing")).isNull();
        for (Constructor<?> constructor : Settings.class.getDeclaredConstructors()) {
            assertThat(Modifier.isPublic(constructor.getModifiers())).isFalse();
        }
    }
}
