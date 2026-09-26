package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TableSizeTest {

    @Test
    void roundsUp() {
        assertThat(TableSize.tableSizeFor(10)).isEqualTo(16);
        assertThat(TableSize.tableSizeFor(17)).isEqualTo(32);
        assertThat(TableSize.tableSizeFor(100)).isEqualTo(128);
        assertThat(TableSize.tableSizeFor(1334)).isEqualTo(2048);
        assertThat(TableSize.tableSizeFor(3)).isEqualTo(4);
    }

    @Test
    void powerOfTwoStays() {
        assertThat(TableSize.tableSizeFor(16)).isEqualTo(16);
        assertThat(TableSize.tableSizeFor(64)).isEqualTo(64);
        assertThat(TableSize.tableSizeFor(2048)).isEqualTo(2048);
        assertThat(TableSize.tableSizeFor(1)).isEqualTo(1);
    }

    @Test
    void zeroOrLessGivesOne() {
        assertThat(TableSize.tableSizeFor(0)).isEqualTo(1);
        assertThat(TableSize.tableSizeFor(-5)).isEqualTo(1);
    }

    @Test
    void largePowersOfTwoStay() {
        assertThat(TableSize.tableSizeFor(1 << 29)).isEqualTo(1 << 29);
        assertThat(TableSize.tableSizeFor(1 << 30)).isEqualTo(1 << 30);
        assertThat(TableSize.tableSizeFor((1 << 29) + 1)).isEqualTo(1 << 30);
        assertThat(TableSize.tableSizeFor(1)).isEqualTo(1);
        assertThat(TableSize.tableSizeFor(2)).isEqualTo(2);
    }
}
