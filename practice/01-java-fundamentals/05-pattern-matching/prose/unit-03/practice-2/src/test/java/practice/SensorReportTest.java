package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static practice.SensorReport.Outage;
import static practice.SensorReport.Reading;

class SensorReportTest {

    /** The failure behind a thrown exception, whether or not something wrapped it once. */
    private static Throwable underlying(Throwable t) {
        return t instanceof MatchException && t.getCause() != null ? t.getCause() : t;
    }

    @Test
    void listsEveryEventOfCalibratedSensors() {
        List<String> lines = SensorReport.report(List.of(
                new Reading("kiln-4", 812.5), new Outage("dock-1", 17), new Reading("cellar-2", -3.25)));

        assertThat(lines).containsExactly("kiln-4 812.5 C", "dock-1 offline 17 min", "cellar-2 -3.25 C");
    }

    @Test
    void anUncalibratedSensorDoesNotStopTheReport() {
        Throwable thrown = catchThrowable(() -> SensorReport.report(List.of(
                new Reading("kiln-4", 790.0), new Reading("boiler-9", Double.NaN), new Outage("dock-1", 4))));

        assertThat(thrown).isNull();
    }

    @Test
    void theUncalibratedSensorIsNamedInItsLine() {
        List<String> lines = SensorReport.report(List.of(
                new Outage("dock-3", 2), new Reading("boiler-9", Double.NaN), new Reading("kiln-4", 790.0)));

        assertThat(lines).containsExactly("dock-3 offline 2 min", "boiler-9 uncalibrated", "kiln-4 790.0 C");
    }

    @Test
    void aReadingNoThermometerCanTakeStillFailsTheReport() {
        Throwable thrown = catchThrowable(() -> SensorReport.report(List.of(
                new Reading("kiln-4", 790.0), new Reading("cellar-2", -400.0))));

        assertThat(thrown).isNotNull();
        assertThat(underlying(thrown)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("absolute zero");
    }
}
