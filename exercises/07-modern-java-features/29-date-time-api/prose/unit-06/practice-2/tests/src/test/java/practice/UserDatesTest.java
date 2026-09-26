package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class UserDatesTest {

    private Locale saved;

    @BeforeEach
    void aServerInTheUs() {
        saved = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheLocale() {
        Locale.setDefault(saved);
    }

    @Test
    void readsEachAcceptedForm() {
        LocalDate ides = LocalDate.of(2024, 3, 15);
        assertThat(UserDates.parse("2024-03-15")).contains(ides);
        assertThat(UserDates.parse("03/15/2024")).contains(ides);
        assertThat(UserDates.parse("15-Mar-2024")).contains(ides);
        assertThat(UserDates.parse("15-Sep-2024")).contains(LocalDate.of(2024, 9, 15));
        assertThat(UserDates.parse("02/29/2024")).contains(LocalDate.of(2024, 2, 29));
        assertThat(UserDates.parse("2024/03/15")).isEmpty();
        assertThat(UserDates.parse("15.03.2024")).isEmpty();
        assertThat(UserDates.parse("")).isEmpty();
    }

    @Test
    void aDayTheMonthDoesNotHaveIsRefused() {
        assertThat(UserDates.parse("2024-02-30")).isEmpty();
        assertThat(UserDates.parse("04/31/2024")).isEmpty();
        assertThat(UserDates.parse("29-Feb-2023")).isEmpty();
    }

    @Test
    void aMissingInputIsEmpty() {
        assertThat(UserDates.parse(null)).isEmpty();
    }

    @Test
    void monthNamesAreEnglishOnAnyServer() {
        Locale.setDefault(Locale.GERMANY);
        assertThat(UserDates.parse("15-Mar-2024")).contains(LocalDate.of(2024, 3, 15));
        assertThat(UserDates.parse("01-Oct-2024")).contains(LocalDate.of(2024, 10, 1));
    }
}
