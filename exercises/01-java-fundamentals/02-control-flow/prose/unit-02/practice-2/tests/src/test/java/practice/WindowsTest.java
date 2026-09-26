package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WindowsTest {

    @Test
    void findsTheLargestWindow() {
        assertThat(Windows.best(new int[]{1, 5, 2, 3, 1}, 2)).isEqualTo(7);
        assertThat(Windows.best(new int[]{2, 8, 1, 1}, 3)).isEqualTo(11);
    }

    @Test
    void theLastWindowCounts() {
        assertThat(Windows.best(new int[]{4, 1, 1, 1, 9}, 2)).isEqualTo(10);
        assertThat(Windows.best(new int[]{3, 2}, 2)).isEqualTo(5);
    }

    @Test
    void allNegativeWindowsStillHaveABest() {
        assertThat(Windows.best(new int[]{-4, -1, -7}, 1)).isEqualTo(-1);
        assertThat(Windows.best(new int[]{-4, -1, -7}, 2)).isEqualTo(-5);
    }
}
