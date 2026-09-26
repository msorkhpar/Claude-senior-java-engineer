package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LatencyStatsTest {

    @Test
    void countsAndTracksTheMaximum() throws InterruptedException {
        LatencyStats stats = new LatencyStats();
        stats.record(12);
        stats.record(40);
        stats.record(7);
        assertThat(stats.count()).isEqualTo(3);
        assertThat(stats.max()).isEqualTo(40);

        LatencyStats shared = new LatencyStats();
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> recorders = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            Thread recorder = new Thread(() -> {
                try {
                    go.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                for (int i = 0; i < 1_000; i++) {
                    shared.record(i);
                }
            });
            recorder.setDaemon(true);
            recorder.start();
            recorders.add(recorder);
        }
        go.countDown();
        for (Thread recorder : recorders) {
            recorder.join(5_000);
            assertThat(recorder.isAlive()).as("a recording thread finished").isFalse();
        }
        assertThat(shared.count()).isEqualTo(4_000);
        assertThat(shared.max()).isEqualTo(999);
    }

    @Test
    void allNegativeValuesKeepTheirMaximum() {
        LatencyStats stats = new LatencyStats();
        stats.record(-5);
        stats.record(-2);
        assertThat(stats.max()).isEqualTo(-2);
    }

    @Test
    void nothingRecordedHasNoMaximum() {
        LatencyStats stats = new LatencyStats();
        assertThat(stats.count()).isZero();
        assertThat(stats.max()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void resetStartsOver() {
        LatencyStats stats = new LatencyStats();
        stats.record(40);
        stats.reset();
        assertThat(stats.count()).isZero();
        assertThat(stats.max()).isEqualTo(Long.MIN_VALUE);
        stats.record(-3);
        assertThat(stats.max()).isEqualTo(-3);
    }

    @Test
    void theStatisticsLiveInTheAddersWithoutALock() {
        int adders = 0;
        int accumulators = 0;
        for (Field field : LatencyStats.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            assertThat(Modifier.isFinal(field.getModifiers())).as(field.getName() + " is final").isTrue();
            assertThat(field.getType()).as(field.getName() + " is a LongAdder or a LongAccumulator")
                    .isIn(LongAdder.class, LongAccumulator.class);
            adders += field.getType() == LongAdder.class ? 1 : 0;
            accumulators += field.getType() == LongAccumulator.class ? 1 : 0;
        }
        assertThat(adders).as("the count is a LongAdder").isEqualTo(1);
        assertThat(accumulators).as("the maximum is a LongAccumulator").isEqualTo(1);
        for (Method method : LatencyStats.class.getDeclaredMethods()) {
            assertThat(Modifier.isSynchronized(method.getModifiers())).as(method.getName() + " takes no lock").isFalse();
        }
    }
}
