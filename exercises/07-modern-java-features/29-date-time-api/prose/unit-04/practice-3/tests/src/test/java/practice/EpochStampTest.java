package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EpochStampTest {

    private static String stamp(long seconds, String fraction) {
        return seconds + "." + fraction;
    }

    @Test
    void roundTripsWholeMilliseconds() {
        Instant instant = Instant.ofEpochSecond(1_710_523_800L, 123_000_000);
        assertThat(EpochStamp.encode(instant)).isEqualTo(stamp(1_710_523_800L, "123000000"));
        assertThat(EpochStamp.decode(stamp(1_710_523_800L, "123000000"))).isEqualTo(instant);
        Instant later = Instant.ofEpochMilli(1_000_999L);
        assertThat(EpochStamp.encode(later)).isEqualTo(stamp(1000, "999000000"));
        assertThat(EpochStamp.decode(stamp(1000, "999000000"))).isEqualTo(later);
    }

    @Test
    void subMillisecondDigitsSurvive() {
        Instant instant = Instant.ofEpochSecond(1_710_523_800L, 123_456_789);
        assertThat(EpochStamp.encode(instant)).isEqualTo(stamp(1_710_523_800L, "123456789"));
        assertThat(EpochStamp.decode(stamp(1_710_523_800L, "123456789"))).isEqualTo(instant);
    }

    @Test
    void theFractionHasNineDigits() {
        assertThat(EpochStamp.encode(Instant.ofEpochSecond(100, 5))).isEqualTo(stamp(100, "000000005"));
        assertThat(EpochStamp.encode(Instant.EPOCH)).isEqualTo(stamp(0, "000000000"));
        assertThat(EpochStamp.encode(Instant.ofEpochSecond(100, 120_000_000))).isEqualTo(stamp(100, "120000000"));
    }

    @Test
    void beforeTheEpochIsNegative() {
        Instant halfBefore = Instant.ofEpochSecond(-1, 500_000_000);
        assertThat(EpochStamp.encode(halfBefore)).isEqualTo("-0.500000000");
        assertThat(EpochStamp.decode("-0.500000000")).isEqualTo(halfBefore);
        assertThat(EpochStamp.decode("-1.250000000")).isEqualTo(Instant.ofEpochMilli(-1250));
    }
}
