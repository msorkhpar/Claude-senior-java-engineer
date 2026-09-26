package practice;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingTest {

    @Test
    void readingsWithTheSameValuesAreEqual() {
        Reading a = new Reading("s1", new double[] {1.5, 2.0});
        assertThat(a.equals(new Reading("s1", new double[] {1.5, 2.0}))).isTrue();
        assertThat(a.equals(new Reading("s1", new double[] {1.5, 2.5}))).isFalse();
        assertThat(a.equals(new Reading("s2", new double[] {1.5, 2.0}))).isFalse();
        assertThat(a.values()).containsExactly(1.5, 2.0);
    }

    @Test
    void equalReadingsHashAlike() {
        Reading a = new Reading("s1", new double[] {1.5, 2.0});
        Reading b = new Reading("s1", new double[] {1.5, 2.0});
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        Set<Reading> set = new HashSet<>();
        set.add(a);
        set.add(b);
        assertThat(set.size()).isEqualTo(1);
    }

    @Test
    void toStringShowsTheValues() {
        assertThat(new Reading("s1", new double[] {1.5, 2.0}).toString())
                .isEqualTo("Reading[sensor=s1, values=[1.5, 2.0]]");
    }

    @Test
    void theCallersArrayIsCopiedIn() {
        double[] raw = {1.5, 2.0};
        Reading reading = new Reading("s1", raw);
        raw[0] = 99;
        assertThat(reading.values()).containsExactly(1.5, 2.0);
    }

    @Test
    void theAccessorHandsOutACopy() {
        Reading reading = new Reading("s1", new double[] {1.5, 2.0});
        reading.values()[1] = 99;
        assertThat(reading.values()).containsExactly(1.5, 2.0);
    }
}
