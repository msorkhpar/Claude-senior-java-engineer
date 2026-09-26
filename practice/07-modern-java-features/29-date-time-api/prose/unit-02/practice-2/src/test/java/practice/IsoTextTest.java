package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IsoTextTest {

    private static final String DATE = String.join("-", "2024", "03", "15");

    @Test
    void readsDatesTimesAndInstants() {
        assertThat(IsoText.read(DATE)).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(IsoText.read("14:30")).isEqualTo(LocalTime.of(14, 30));
        assertThat(IsoText.read("14:30:15")).isEqualTo(LocalTime.of(14, 30, 15));
        assertThat(IsoText.read(DATE + "T14:30:00.5")).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30, 0, 500_000_000));
        assertThat(IsoText.read(DATE + "T14:30")).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30));
        assertThat(IsoText.read(DATE + "T14:30:00Z"))
                .isEqualTo(Instant.ofEpochSecond(LocalDateTime.of(2024, 3, 15, 14, 30).toEpochSecond(ZoneOffset.UTC)));
    }

    @Test
    void anOffsetMakesAnOffsetDateTime() {
        assertThat(IsoText.read(DATE + "T14:30:00+05:30"))
                .isInstanceOf(OffsetDateTime.class)
                .isEqualTo(OffsetDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneOffset.ofHoursMinutes(5, 30)));
        assertThat(IsoText.read(DATE + "T14:30:00-04:00"))
                .isEqualTo(OffsetDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneOffset.ofHours(-4)));
    }

    @Test
    void aZoneIdMakesAZonedDateTime() {
        Object read = IsoText.read(DATE + "T14:30:00+05:30[Asia/Kolkata]");
        assertThat(read).isInstanceOf(ZonedDateTime.class).hasToString("2024-03-15T14:30+05:30[Asia/Kolkata]");
        assertThat(read)
                .isEqualTo(ZonedDateTime.of(2024, 3, 15, 14, 30, 0, 0, ZoneId.of("Asia/Kolkata")));
        Object zurich = IsoText.read(DATE + "T09:15:00+01:00[Europe/Zurich]");
        assertThat(zurich).isInstanceOf(ZonedDateTime.class).hasToString("2024-03-15T09:15+01:00[Europe/Zurich]");
    }

    @Test
    void amountsAreDurationsOrPeriods() {
        assertThat(IsoText.read("PT2H30M")).isEqualTo(Duration.ofMinutes(150));
        assertThat(IsoText.read("P1Y2M3D")).isEqualTo(Period.of(1, 2, 3));
        assertThat(IsoText.read("P1DT2H")).isEqualTo(Duration.ofHours(26));
    }
}
