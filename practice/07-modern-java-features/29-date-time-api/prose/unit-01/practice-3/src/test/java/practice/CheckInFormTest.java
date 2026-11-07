package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CheckInFormTest {

    @Test
    void readsAWellFormedDate() {
        assertThat(CheckInForm.checkInDate("2025-11-04")).isEqualTo(LocalDate.of(2025, 11, 4));
        assertThat(CheckInForm.checkInDate("2028-02-29")).isEqualTo(LocalDate.of(2028, 2, 29));
        assertThat(CheckInForm.review("2025-12-31")).isEqualTo("Check-in on WEDNESDAY 2025-12-31");
        assertThatThrownBy(() -> CheckInForm.checkInDate("next friday"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aDayThatDoesNotExistIsRefused() {
        assertThatThrownBy(() -> CheckInForm.checkInDate("2025-04-31")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckInForm.checkInDate("2027-02-29")).isInstanceOf(IllegalArgumentException.class);
        assertThat(CheckInForm.review("2025-06-31")).isEqualTo("Please enter the date as YYYY-MM-DD");
    }

    @Test
    void textAfterTheDateIsRefused() {
        assertThatThrownBy(() -> CheckInForm.checkInDate("2025-11-04 14:00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckInForm.checkInDate("2025-11-045")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMonthOrDayWithoutItsLeadingZeroIsRefused() {
        assertThatThrownBy(() -> CheckInForm.checkInDate("2025-7-14")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckInForm.checkInDate("2025-07-4")).isInstanceOf(IllegalArgumentException.class);
    }
}
