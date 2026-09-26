package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateInputTest {

    private static String text(String year, String month, String day) {
        return String.join("-", year, month, day);
    }

    @Test
    void readsAValidDate() {
        assertThat(DateInput.parse(text("2024", "03", "15"))).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(DateInput.parse(text("1999", "12", "31"))).isEqualTo(LocalDate.of(1999, 12, 31));
        assertThat(DateInput.parse(text("2024", "02", "29"))).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void monthThirteenIsRefused() {
        assertThatThrownBy(() -> DateInput.parse(text("2024", "13", "01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void februaryThirtiethIsRefused() {
        assertThatThrownBy(() -> DateInput.parse(text("2024", "02", "30")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DateInput.parse(text("2023", "02", "29")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyTheExactFormIsAccepted() {
        for (String text : new String[] {"+12024-03-15", "-2024-03-15", "2024-3-5", "2024-3-15", "24-03-15",
                " " + text("2024", "03", "15"), text("2024", "03", "15") + " "}) {
            assertThatThrownBy(() -> DateInput.parse(text)).as(text).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void trailingTextIsRefused() {
        assertThatThrownBy(() -> DateInput.parse(text("2024", "03", "15") + " extra"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DateInput.parse(text("2024", "03", "15") + "T10:00"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
