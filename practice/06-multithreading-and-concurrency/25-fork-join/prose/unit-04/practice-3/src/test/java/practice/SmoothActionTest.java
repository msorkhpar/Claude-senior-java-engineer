package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SmoothActionTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static final int[] RAMP = {3, 6, 9, 12, 15, 18, 21, 24};

    @Test
    void smoothsTheInterior() {
        int[] source = {0, 9, 0, 9, 0};
        int[] dest = new int[5];
        pool.invoke(new SmoothAction(source, dest, 0, 5, 5));
        assertThat(Arrays.copyOfRange(dest, 1, 4)).containsExactly(3, 6, 3);
        assertThat(source).as("the source is unchanged").containsExactly(0, 9, 0, 9, 0);
    }

    @Test
    void neighboursAcrossALeafBoundary() {
        int[] source = {0, 9, 0, 9, 0, 9, 0, 9};
        int[] dest = new int[8];
        pool.invoke(new SmoothAction(source, dest, 0, 8, 2));
        assertThat(dest).containsExactly(0, 3, 6, 3, 6, 3, 6, 9);
    }

    @Test
    void theEndsAreCopied() {
        int[] source = {5, 1, 1, 1, 7};
        int[] dest = new int[5];
        pool.invoke(new SmoothAction(source, dest, 0, 5, 5));
        assertThat(dest[0]).isEqualTo(5);
        assertThat(dest[4]).isEqualTo(7);
    }

    @Test
    void writesOnlyItsOwnRange() {
        int[] source = RAMP.clone();
        int[] dest = new int[8];
        pool.invoke(new SmoothAction(source, dest, 2, 5, 2));
        assertThat(dest).containsExactly(0, 0, 9, 12, 15, 0, 0, 0);
    }

    @Test
    void divisionTruncates() {
        int[] source = {0, 2, 3, -5, 0};
        int[] dest = new int[5];
        pool.invoke(new SmoothAction(source, dest, 0, 5, 5));
        assertThat(dest).as("5 / 3 is 1 and -2 / 3 is 0: Java's integer division, which truncates").containsExactly(0, 1, 0, 0, 0);
    }
}
