package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BucketsTest {

    /** A key whose hashCode is exactly h. */
    private record Hashed(int h) {
        @Override
        public int hashCode() {
            return h;
        }
    }

    @Test
    void masksTheSpreadHash() {
        assertThat(Buckets.index(new Hashed(42), 16)).isEqualTo(10);
        assertThat(Buckets.index(new Hashed(58), 16)).isEqualTo(10);
        assertThat(Buckets.index(new Hashed(42), 32)).isEqualTo(10);
        assertThat(Buckets.index(new Hashed(58), 32)).isEqualTo(26);
        assertThat(Buckets.index(Integer.valueOf(1000), 64)).isEqualTo(1000 & 63);
    }

    @Test
    void negativeHashGivesAValidIndex() {
        // -7 spreads to 0xFFFF0006, whose low four bits are 6
        assertThat(Buckets.index(new Hashed(-7), 16)).isEqualTo(6);
        assertThat(Buckets.index(new Hashed(Integer.MIN_VALUE), 16)).isEqualTo(0);
        // in a table of 2^20 buckets the folded-in upper bits must be the unsigned ones
        assertThat(Buckets.index(new Hashed(-7), 1 << 20)).isEqualTo(0xF0006);
    }

    @Test
    void upperBitsAreSpread() {
        assertThat(Buckets.index(new Hashed(0x10000), 16)).isEqualTo(1);
        assertThat(Buckets.index(new Hashed(0x30000), 16)).isEqualTo(3);
        assertThat(Buckets.index(new Hashed(0), 16)).isEqualTo(0);
    }

    @Test
    void nullKeyGoesToBucketZero() {
        assertThat(Buckets.index(null, 16)).isZero();
        assertThat(Buckets.index(null, 1024)).isZero();
    }
}
