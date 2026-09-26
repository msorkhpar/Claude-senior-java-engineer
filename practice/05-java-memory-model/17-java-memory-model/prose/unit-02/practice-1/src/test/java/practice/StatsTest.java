package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class StatsTest {

    /** Yields 10, then pauses (saying so) until released, then yields 20. */
    private static Iterable<Integer> pausingAfterTheFirst(CountDownLatch paused, CountDownLatch go) {
        return () -> new Iterator<>() {
            private int given;

            @Override
            public boolean hasNext() {
                if (given == 1) {
                    paused.countDown();
                    try {
                        go.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return given < 2;
            }

            @Override
            public Integer next() {
                if (given >= 2) {
                    throw new NoSuchElementException();
                }
                given++;
                return given == 1 ? 10 : 20;
            }
        };
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void sumsTheValues() {
        Stats stats = new Stats();
        assertThat(stats.sum(List.of(1, 2, 3))).isEqualTo(6);
        assertThat(stats.sum(List.of())).isZero();
        assertThat(stats.sum(List.of(Integer.MAX_VALUE, 1))).isEqualTo(2_147_483_648L);
    }

    @Test
    void twoCallsAtOnceDoNotMix() throws Exception {
        Stats shared = new Stats();
        CountDownLatch paused = new CountDownLatch(1);
        CountDownLatch go = new CountDownLatch(1);
        AtomicLong first = new AtomicLong(-1);
        AtomicLong second = new AtomicLong(-1);
        Thread a = daemon(() -> first.set(shared.sum(pausingAfterTheFirst(paused, go))));
        assertThat(paused.await(5, TimeUnit.SECONDS)).as("thread A is half way through its sum").isTrue();
        Thread b = daemon(() -> second.set(shared.sum(List.of(1, 2, 3))));
        b.join(2000);
        boolean bWaited = b.isAlive();
        go.countDown();
        a.join(5000);
        b.join(5000);
        assertThat(bWaited).as("thread B had to wait for thread A").isFalse();
        assertThat(a.isAlive()).isFalse();
        assertThat(second.get()).isEqualTo(6);
        assertThat(first.get()).isEqualTo(30);
    }

    @Test
    void keepsNoStateInFields() {
        Stats stats = new Stats();
        assertThat(stats.sum(List.of(4, 5))).isEqualTo(9);
        for (Field f : Stats.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {
                assertThat(Modifier.isFinal(f.getModifiers()))
                        .as("field " + f.getName() + " is shared by every thread using this Stats")
                        .isTrue();
            }
        }
    }
}
