package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LetterDateTest {

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
    void writesAFullUsDate() {
        assertThat(LetterDate.format(LocalDate.of(2024, 3, 15), Locale.US)).isEqualTo("Friday, March 15, 2024");
        assertThat(LetterDate.format(LocalDate.of(2024, 7, 4), Locale.US)).isEqualTo("Thursday, July 4, 2024");
    }

    @Test
    void theLocaleDecidesTheOrder() {
        assertThat(LetterDate.format(LocalDate.of(2024, 3, 15), Locale.FRANCE)).isEqualTo("vendredi 15 mars 2024");
        assertThat(LetterDate.format(LocalDate.of(2024, 3, 15), Locale.GERMANY)).isEqualTo("Freitag, 15. März 2024");
    }

    @Test
    void theServerLocaleIsIgnored() {
        Locale.setDefault(Locale.GERMANY);
        assertThat(LetterDate.format(LocalDate.of(2024, 3, 15), Locale.US)).isEqualTo("Friday, March 15, 2024");
    }
}
