package com.github.msorkhpar.claudejavatutor.base;

import com.github.msorkhpar.claudejavatutor.base.PerformanceTestUtil.MeasurementResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTestUtilTest {

    @Test
    void measureExecution_returnsTheResultAndANonNegativeTime() {
        var measured = PerformanceTestUtil.measureExecution(() -> 21 * 2);
        assertThat(measured.result()).isEqualTo(42);
        assertThat(measured.executionTime()).isNotNegative();
    }

    @Test
    void equals_comparesResultsAndIgnoresExecutionTime() {
        var a = new MeasurementResult<>(10L, "same");
        var b = new MeasurementResult<>(99L, "same");
        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(a);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(new MeasurementResult<>(10L, "other"));
    }

    @Test
    void equals_isSymmetricWithABareResultValue() {
        var measured = new MeasurementResult<>(10L, 42L);
        // Long.equals(measured) is false, so measured.equals(42L) must be false too
        assertThat(Long.valueOf(42L).equals(measured)).isFalse();
        assertThat(measured.equals(42L)).isFalse();
    }

    @Test
    void equals_handlesNullResults() {
        var a = new MeasurementResult<>(1L, null);
        var b = new MeasurementResult<>(2L, null);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(new MeasurementResult<>(1L, "x"));
        assertThat(a.equals(null)).isFalse();
    }
}
