package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class StartupGateTest {

    private final ExecutorService startup = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "startup");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stop() {
        startup.shutdownNow();
    }

    private static Thread daemon(String name, Runnable body) {
        Thread t = new Thread(body, name);
        t.setDaemon(true);
        return t;
    }

    private static Runnable fails(String why) {
        return () -> { throw new IllegalStateException(why); };
    }

    @Test
    void theGateOpensWhenTheLastLoaderFinishes() throws Exception {
        StartupGate gate = new StartupGate(3);
        AtomicReference<Object> answer = new AtomicReference<>();
        Thread waiter = daemon("startup", () -> {
            try {
                answer.set(gate.awaitLoaded(30, TimeUnit.SECONDS));
            } catch (Throwable e) {
                answer.set(e);
            }
        });
        waiter.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (waiter.getState() != Thread.State.TIMED_WAITING && waiter.isAlive() && System.nanoTime() < deadline) {
            Thread.onSpinWait();                          // until the startup thread is waiting
        }

        List<Thread> loaders = new ArrayList<>();
        for (String cache : List.of("fx-rates", "warehouses", "promotions")) {
            loaders.add(daemon(cache, () -> gate.runLoader(cache, () -> { })));
        }
        loaders.forEach(Thread::start);
        waiter.join(10_000);

        assertThat(answer.get()).isEqualTo(Boolean.TRUE);
        assertThat(gate.failed()).isEmpty();
    }

    @Test
    void theGateStaysShutWhileALoaderIsStillRunning() throws Exception {
        StartupGate gate = new StartupGate(3);
        gate.runLoader("fx-rates", () -> { });
        gate.runLoader("warehouses", () -> { });

        Future<Boolean> open = startup.submit(() -> gate.awaitLoaded(100, TimeUnit.MILLISECONDS));

        assertThat(open.get(10, TimeUnit.SECONDS)).isFalse();
    }

    @Test
    void loadersThatFinishedBeforeAnyoneWaitedStillOpenTheGate() throws Exception {
        StartupGate gate = new StartupGate(3);
        gate.runLoader("fx-rates", () -> { });
        gate.runLoader("warehouses", () -> { });
        gate.runLoader("promotions", () -> { });

        Future<Boolean> open = startup.submit(() -> gate.awaitLoaded(10, TimeUnit.MINUTES));

        assertThat(open.get(5, TimeUnit.SECONDS)).as("answered without waiting out the 10 minutes").isTrue();
    }

    @Test
    void aLoaderThatFailsLetsTheInstanceStartWithoutItsCache() throws Exception {
        StartupGate gate = new StartupGate(3);
        gate.runLoader("fx-rates", () -> { });
        gate.runLoader("tax-rules", fails("tax service unreachable"));
        gate.runLoader("promotions", () -> { });

        Future<Boolean> open = startup.submit(() -> gate.awaitLoaded(2, TimeUnit.SECONDS));

        assertThat(open.get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(gate.failed()).containsExactly("tax-rules");
    }
}
