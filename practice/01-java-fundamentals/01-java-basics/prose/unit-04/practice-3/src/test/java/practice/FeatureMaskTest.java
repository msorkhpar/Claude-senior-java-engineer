package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureMaskTest {

    @Test
    void switchesLowFeaturesOnAndOff() {
        long mask = FeatureMask.of(1, 6, 13);
        assertThat(mask).isEqualTo(0b10_0000_0100_0010L);
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(6))).isTrue();
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(1, 13))).isTrue();
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(7))).isFalse();

        long off = FeatureMask.disable(mask, FeatureMask.of(6));
        assertThat(FeatureMask.hasAll(off, FeatureMask.of(6))).isFalse();
        assertThat(FeatureMask.hasAll(off, FeatureMask.of(1, 13))).isTrue();
    }

    @Test
    void featuresAboveThirtyOneHaveTheirOwnBits() {
        assertThat(FeatureMask.bit(39)).isEqualTo(549_755_813_888L);
        assertThat(FeatureMask.bit(47)).isEqualTo(140_737_488_355_328L);

        long mask = FeatureMask.of(42);
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(10))).isFalse();
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(42))).isTrue();
        assertThat(Long.bitCount(FeatureMask.of(2, 34, 40))).isEqualTo(3);
    }

    @Test
    void switchingOffAFeatureThatIsOffLeavesItOff() {
        long mask = FeatureMask.of(3, 20);
        long off = FeatureMask.disable(mask, FeatureMask.of(3, 9));

        assertThat(off).isEqualTo(FeatureMask.of(20));
        assertThat(FeatureMask.hasAll(off, FeatureMask.of(9))).isFalse();
    }

    @Test
    void aBundleNeedsEveryOneOfItsFeatures() {
        long mask = FeatureMask.of(4, 17);

        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(4, 5))).isFalse();
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(0, 17, 30))).isFalse();
        assertThat(FeatureMask.hasAll(mask, FeatureMask.of(4, 17))).isTrue();
    }
}
