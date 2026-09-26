package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaysTest {

    @Test
    void namesWeekendsWeekdaysAndOtherWords() {
        assertThat(Days.type("Saturday")).isEqualTo("Weekend");
        assertThat(Days.type("Sunday")).isEqualTo("Weekend");
        assertThat(Days.type("Monday")).isEqualTo("Weekday");
        assertThat(Days.type("Wednesday")).isEqualTo("Weekday");
        assertThat(Days.type("Someday")).isEqualTo("Invalid day");
    }

    @Test
    void nullIsInvalidInput() {
        assertThat(Days.type(null)).isEqualTo("Invalid input");
    }

    @Test
    void caseDoesNotMatter() {
        assertThat(Days.type("sunday")).isEqualTo("Weekend");
        assertThat(Days.type("friday")).isEqualTo("Weekday");
        assertThat(Days.type("MONDAY")).isEqualTo("Weekday");
        assertThat(Days.type("sAtUrDaY")).isEqualTo("Weekend");
    }
}
