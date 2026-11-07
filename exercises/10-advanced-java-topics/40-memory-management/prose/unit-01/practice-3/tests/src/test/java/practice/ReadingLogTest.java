package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingLogTest {

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static final long MIDNIGHT = 1_767_225_600_000L;     // 2026-01-01T00:00Z
    private static final int DAY = 172_800;                       // one reading every 500 ms

    private static long timeOf(int i) {
        return MIDNIGHT + 500L * i;
    }

    /** Swings between 15.0 and 25.0, so about half the readings are above 20.0. */
    private static double valueOf(int i) {
        return 20.0 + 5.0 * Math.sin(i / 300.0) + (i % 7) * 0.001;
    }

    private static ReadingLog fullDay() {
        ReadingLog log = new ReadingLog(DAY);
        for (int i = 0; i < DAY; i++) log.append(timeOf(i), valueOf(i));
        return log;
    }

    @Test
    void readsBackEveryReadingInArrivalOrder() {
        ReadingLog log = new ReadingLog(4);
        log.append(1_700_000_000_123L, 18.25);
        log.append(1_700_000_000_623L, -3.5);
        log.append(1_700_000_001_123L, 1e9);

        assertThat(log.size()).isEqualTo(3);
        assertThat(log.timeAt(0)).isEqualTo(1_700_000_000_123L);
        assertThat(log.timeAt(2)).isEqualTo(1_700_000_001_123L);
        assertThat(log.valueAt(1)).isEqualTo(-3.5);
        assertThat(log.valueAt(2)).isEqualTo(1e9);
    }

    @Test
    void findsTheTimesAboveALimitInArrivalOrder() {
        ReadingLog log = new ReadingLog(6);
        log.append(1_700_000_000_000L, 21.0);
        log.append(1_700_000_000_500L, 19.0);
        log.append(1_700_000_001_000L, 20.0);
        log.append(1_700_000_001_500L, 20.5);
        log.append(1_700_000_002_000L, 35.0);

        assertThat(log.timesAbove(20.0)).containsExactly(1_700_000_000_000L, 1_700_000_001_500L, 1_700_000_002_000L);
        assertThat(log.timesAbove(40.0)).isEmpty();
    }

    @Test
    void aDayOfReadingsTakesAtMostSeventeenBytesEach() {
        fullDay();                                                // load and warm up first
        long before = THREADS.getCurrentThreadAllocatedBytes();
        ReadingLog log = fullDay();
        long allocated = THREADS.getCurrentThreadAllocatedBytes() - before;

        assertThat(log.size()).isEqualTo(DAY);
        assertThat(log.valueAt(DAY - 1)).isEqualTo(valueOf(DAY - 1));
        assertThat(allocated / (double) DAY).as("bytes allocated per reading").isLessThanOrEqualTo(17.0);
    }

    @Test
    void aQueryAllocatesLittleBeyondItsAnswer() {
        ReadingLog log = fullDay();
        int expected = 0;
        for (int i = 0; i < DAY; i++) if (valueOf(i) > 20.0) expected++;
        log.timesAbove(20.0);                                     // warm up

        long before = THREADS.getCurrentThreadAllocatedBytes();
        long[] times = log.timesAbove(20.0);
        long allocated = THREADS.getCurrentThreadAllocatedBytes() - before;

        assertThat(times).hasSize(expected);
        assertThat(expected).isBetween(DAY / 3, 2 * DAY / 3);
        assertThat(allocated).as("bytes allocated by one query, for %d matching readings", expected)
                .isLessThanOrEqualTo(8L * DAY + 8L * expected + 4096);
    }
}
