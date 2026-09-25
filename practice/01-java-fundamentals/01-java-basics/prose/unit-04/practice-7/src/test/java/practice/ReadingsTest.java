package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingsTest {

    @Test
    void findsTheLargestReading() {
        assertThat(Readings.largest(new double[]{1.5, 3.25, 2.0})).isEqualTo(3.25);
        assertThat(Readings.largest(new double[]{7.0})).isEqualTo(7.0);
    }

    @Test
    void allReadingsMayBeNegative() {
        assertThat(Readings.largest(new double[]{-5.0, -3.0, -9.0})).isEqualTo(-3.0);
    }

    @Test
    void failedReadingsAreIgnored() {
        assertThat(Readings.largest(new double[]{1.0, Double.NaN, 4.0})).isEqualTo(4.0);
        assertThat(Readings.largest(new double[]{Double.NaN, 2.0})).isEqualTo(2.0);
    }

    @Test
    void noRealReadingIsNaN() {
        assertThat(Double.isNaN(Readings.largest(new double[0]))).isTrue();
        assertThat(Double.isNaN(Readings.largest(new double[]{Double.NaN, Double.NaN}))).isTrue();
    }
}
