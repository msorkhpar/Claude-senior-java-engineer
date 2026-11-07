package practice;

import org.junit.jupiter.api.Test;
import practice.Readings.Reading;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingsTest {

    private static Reading r(String sensor, double value, String unit, long takenAt) {
        return new Reading(sensor, value, unit, takenAt);
    }

    @Test
    void oneReadingPerSensorGivesThatReadingAndItsUnit() {
        Reading soil = r("bed-3/soil", 41.5, "%", 1_700_000_100L);
        Reading air = r("bed-3/air", 22.25, "C", 1_700_000_160L);
        Reading light = r("roof/light", 840.0, "lx", 1_700_000_040L);
        List<Reading> batch = List.of(soil, air, light);

        assertThat(Readings.latestBySensor(batch))
                .isEqualTo(Map.of("bed-3/soil", soil, "bed-3/air", air, "roof/light", light));
        assertThat(Readings.unitBySensor(batch))
                .isEqualTo(Map.of("bed-3/soil", "%", "bed-3/air", "C", "roof/light", "lx"));
        assertThat(Readings.dashboard(batch))
                .containsExactly("bed-3/air: 22.25 C", "bed-3/soil: 41.5 %", "roof/light: 840.0 lx");
    }

    @Test
    void aSensorThatReportsSeveralTimesGetsOneEntry() {
        Reading first = r("tank/level", 0.81, "m", 1_700_000_000L);
        Reading second = r("tank/level", 0.78, "m", 1_700_000_300L);
        Reading third = r("tank/level", 0.74, "m", 1_700_000_600L);
        Reading pump = r("tank/pump", 1.0, "on", 1_700_000_200L);
        List<Reading> batch = List.of(first, pump, second, third);

        assertThat(Readings.latestBySensor(batch)).isEqualTo(Map.of("tank/level", third, "tank/pump", pump));
        assertThat(Readings.unitBySensor(batch)).isEqualTo(Map.of("tank/level", "m", "tank/pump", "on"));
    }

    @Test
    void aRetriedReadingThatArrivesLateDoesNotHideANewerOne() {
        Reading newer = r("vent/2", 35.0, "deg", 1_700_000_900L);
        Reading older = r("vent/2", 10.0, "deg", 1_700_000_500L);
        Reading oldest = r("vent/2", 0.0, "deg", 1_700_000_100L);
        List<Reading> batch = List.of(oldest, newer, older);

        assertThat(Readings.latestBySensor(batch)).isEqualTo(Map.of("vent/2", newer));
    }

    @Test
    void anUncalibratedSensorHasNoUnitButStillShowsItsLatestValue() {
        Reading fresh = r("bed-7/soil", 12.0, null, 1_700_000_050L);
        Reading again = r("bed-7/soil", 13.5, null, 1_700_000_350L);
        Reading air = r("bed-7/air", 19.0, "C", 1_700_000_200L);
        List<Reading> batch = List.of(fresh, air, again);

        assertThat(Readings.unitBySensor(batch)).isEqualTo(Map.of("bed-7/air", "C"));
        assertThat(Readings.dashboard(batch))
                .containsExactly("bed-7/air: 19.0 C", "bed-7/soil: 13.5 (uncalibrated)");
    }
}
