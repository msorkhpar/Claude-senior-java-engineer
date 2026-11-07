package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SampleWindowsTest {

    private static final long SLOW = 800;

    /** Two-second windows, a ring of five slots, 800 ms and above counts as slow. */
    private SampleWindows windows() {
        return new SampleWindows(2_000, 5, SLOW);
    }

    @Test
    void countsSamplesAfterTheRolloutInTheirOwnWindows() {
        SampleWindows w = windows();
        assertThat(w.window(0)).isEqualTo(0);
        assertThat(w.window(1_999)).isEqualTo(0);
        assertThat(w.window(2_000)).isEqualTo(1);
        assertThat(w.window(9_100)).isEqualTo(4);
        assertThat(w.slot(0)).isEqualTo(0);
        assertThat(w.slot(4)).isEqualTo(4);
        assertThat(w.slot(5)).isEqualTo(0);
        assertThat(w.slot(12)).isEqualTo(2);

        w.record(150, 900);
        w.record(1_850, 1_200);
        w.record(2_400, 90);
        assertThat(w.slowPercentOf(0)).isEqualTo(100);
        assertThat(w.slowPercentOf(1)).isEqualTo(0);
        assertThat(w.slowPercentOf(2)).isEqualTo(-1);
    }

    @Test
    void aSampleFromJustBeforeTheRolloutIsCountedInTheWindowBeforeIt() {
        SampleWindows w = windows();
        assertThat(w.window(-1)).isEqualTo(-1);
        assertThat(w.window(-2_000)).isEqualTo(-1);
        assertThat(w.window(-2_001)).isEqualTo(-2);
        assertThat(w.window(-7_300)).isEqualTo(-4);

        w.record(-450, 1_500);
        w.record(450, 120);
        assertThat(w.slowPercentOf(0)).isEqualTo(0);
        assertThat(w.slowPercentOf(-1)).isEqualTo(100);
    }

    @Test
    void windowsBeforeTheRolloutTakeTheirTurnInTheRing() {
        SampleWindows w = windows();
        for (long window = -11; window <= 11; window++) {
            int slot = w.slot(window);
            assertThat(slot).as("slot of window %d", window).isBetween(0, 4);
            assertThat(w.slot(window + 1)).as("slot of window %d", window + 1).isEqualTo((slot + 1) % 5);
        }
    }

    @Test
    void theSlowShareIsRoundedDownNotLost() {
        assertThat(SampleWindows.slowPercent(29, 100)).isEqualTo(29);
        assertThat(SampleWindows.slowPercent(7, 9)).isEqualTo(77);
        assertThat(SampleWindows.slowPercent(1, 3)).isEqualTo(33);
        assertThat(SampleWindows.slowPercent(4_999_999_999L, 5_000_000_000L)).isEqualTo(99);

        SampleWindows w = windows();
        w.record(3_100, 950);
        w.record(3_200, 300);
        w.record(3_300, 310);
        assertThat(w.slowPercentOf(1)).isEqualTo(33);
    }
}
