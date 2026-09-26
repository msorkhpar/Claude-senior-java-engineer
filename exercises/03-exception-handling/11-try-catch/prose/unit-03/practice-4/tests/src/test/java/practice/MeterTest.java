package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeterTest {

    @Test
    void countsTheCallWhileItRuns() {
        Meter meter = new Meter();
        assertThat(meter.inFlight()).isZero();
        assertThat(meter.call(() -> meter.inFlight() * 10)).isEqualTo(10);
        assertThat(meter.call(() -> meter.call(() -> meter.inFlight()))).isEqualTo(2);
        assertThat(meter.inFlight()).isZero();
    }

    @Test
    void aFailureReachesTheCaller() {
        Meter meter = new Meter();
        IllegalStateException down = new IllegalStateException("service down");
        assertThatThrownBy(() -> meter.call(() -> {
            throw down;
        })).isSameAs(down);
    }

    @Test
    void aFailureStillEndsTheCall() {
        Meter meter = new Meter();
        try {
            meter.call(() -> {
                throw new IllegalStateException("service down");
            });
        } catch (IllegalStateException expected) {
            // the task's own failure
        }
        assertThat(meter.inFlight()).isZero();
        assertThat(meter.call(() -> meter.inFlight())).isEqualTo(1);
    }
}
