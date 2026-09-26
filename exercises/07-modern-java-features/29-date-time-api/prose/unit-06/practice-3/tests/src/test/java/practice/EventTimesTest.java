package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTimesTest {

    @Test
    void readsAFullDateAndTime() {
        assertThat(EventTimes.parse("2024-03-15T10:30:45")).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 30, 45));
        assertThat(EventTimes.parse("2024-12-31T23:59:59")).isEqualTo(LocalDateTime.of(2024, 12, 31, 23, 59, 59));
        assertThatThrownBy(() -> EventTimes.parse("2024-03-15 10:30")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> EventTimes.parse("15/03/2024")).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void aDateAloneIsTheStartOfTheDay() {
        assertThat(EventTimes.parse("2024-03-15")).isEqualTo(LocalDateTime.of(2024, 3, 15, 0, 0));
    }

    @Test
    void otherFormsAreRefused() {
        assertThatThrownBy(() -> EventTimes.parse("2024-03-15T10:30:45.5")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> EventTimes.parse("2024-03-15:45")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> EventTimes.parse("2024-02-30")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> EventTimes.parse("2024-03-15T24:00")).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void theSecondsMayBeLeftOut() {
        assertThat(EventTimes.parse("2024-03-15T10:30")).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 30));
    }
}
