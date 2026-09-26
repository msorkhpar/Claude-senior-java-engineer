package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgesTest {

    private static final LocalDate BIRTH = LocalDate.parse("1990-06-15");

    @Test
    void countsWholeYears() {
        assertThat(Ages.ageOn(BIRTH, LocalDate.of(2024, 6, 15))).isEqualTo(34);
        assertThat(Ages.ageOn(BIRTH, LocalDate.of(2024, 12, 1))).isEqualTo(34);
        assertThat(Ages.ageOn(BIRTH, BIRTH)).isEqualTo(0);
        Clock clock = Clock.fixed(Instant.parse("2024-07-01T12:00:00Z"), ZoneOffset.UTC);
        assertThat(Ages.ageToday(BIRTH, clock)).isEqualTo(34);
    }

    @Test
    void theBirthdayMustHaveComeThisYear() {
        assertThat(Ages.ageOn(BIRTH, LocalDate.of(2024, 6, 14))).isEqualTo(33);
        assertThat(Ages.ageOn(BIRTH, LocalDate.of(2025, 1, 1))).isEqualTo(34);
    }

    @Test
    void leapYearsDoNotShiftTheBirthday() {
        assertThat(Ages.ageOn(LocalDate.of(2000, 3, 1), LocalDate.of(2023, 3, 1))).isEqualTo(23);
    }

    @Test
    void todayIsTheClocksDateInItsZone() {
        Clock tokyo = Clock.fixed(Instant.parse("2024-06-14T20:00:00Z"), ZoneId.of("Asia/Tokyo"));
        assertThat(Ages.ageToday(BIRTH, tokyo)).as("it is already June 15 in Tokyo").isEqualTo(34);
    }

    @Test
    void aBirthAfterTheDateIsRefused() {
        assertThatThrownBy(() -> Ages.ageOn(LocalDate.of(2030, 1, 1), LocalDate.of(2024, 6, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
