package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MeterDayTest {

    private static final int G = MeterDay.GAP;

    /** Readings that refuse to be read far more often than a single pass over them needs. */
    private static MeterDay.Readings day(int... wh) {
        int[] reads = new int[1];
        return new MeterDay.Readings() {
            public int size() {
                return wh.length;
            }

            public int get(int minute) {
                if (++reads[0] > 3 * wh.length + 3) {
                    throw new IllegalStateException("minute " + minute + " read again and again: "
                            + reads[0] + " reads of " + wh.length + " readings");
                }
                return wh[minute];
            }
        };
    }

    /** Runs the summary on a daemon thread, so a loop that never ends can't hold the test. */
    private static MeterDay.Summary summarise(MeterDay.Readings readings) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Callable<MeterDay.Summary> job = () -> MeterDay.summarise(readings);
            return pool.submit(job).get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aDayWithEveryReadingIsSummedInFull() throws Exception {
        MeterDay.Summary s = summarise(day(12, 0, 7, 30, 5));

        assertThat(s).isEqualTo(new MeterDay.Summary(54, 0, 0));
    }

    @Test
    void missingMinutesInsideTheDayAreSkippedAndCounted() throws Exception {
        MeterDay.Summary s = summarise(day(8, G, 4, G, G, 9, 2));

        assertThat(s).isEqualTo(new MeterDay.Summary(23, 3, 2));
    }

    @Test
    void anOutageThatLastsUntilMidnightIsTheLongest() throws Exception {
        MeterDay.Summary s = summarise(day(6, G, 3, 11, G, G, G));

        assertThat(s.longestOutage()).isEqualTo(3);
        assertThat(s.gaps()).isEqualTo(4);
        assertThat(s.totalWh()).isEqualTo(20);
    }
}
