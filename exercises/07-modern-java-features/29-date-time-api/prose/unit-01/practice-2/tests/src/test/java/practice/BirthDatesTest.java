package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BirthDatesTest {

    @Test
    void columnsWrittenForADateReadBackAsThatDate() {
        for (LocalDate d : List.of(LocalDate.of(1971, 8, 23), LocalDate.of(2004, 2, 29),
                LocalDate.of(1988, 11, 30), LocalDate.of(2012, 5, 1))) {
            int[] c = BirthDates.toColumns(d);
            assertThat(BirthDates.toLocalDate(c[0], c[1], c[2])).isEqualTo(d);
        }
    }

    @Test
    void theMonthColumnCountsFromZero() {
        assertThat(BirthDates.toLocalDate(83, 6, 9)).isEqualTo(LocalDate.of(1983, 7, 9));
        assertThat(BirthDates.toColumns(LocalDate.of(1983, 7, 9))).containsExactly(83, 6, 9);
        assertThat(BirthDates.toLocalDate(95, 0, 17)).isEqualTo(LocalDate.of(1995, 1, 17));
    }

    @Test
    void theYearColumnCountsFrom1900() {
        assertThat(BirthDates.toLocalDate(61, 3, 12)).isEqualTo(LocalDate.of(1961, 4, 12));
        assertThat(BirthDates.toLocalDate(103, 9, 5)).isEqualTo(LocalDate.of(2003, 10, 5));
        assertThat(BirthDates.toColumns(LocalDate.of(2003, 10, 5))).containsExactly(103, 9, 5);
    }

    @Test
    void aRowThatNamesNoRealDateIsSetAside() {
        List<BirthDates.Row> rejected = new ArrayList<>();
        Map<String, LocalDate> migrated = BirthDates.migrate(List.of(
                new BirthDates.Row("C-310", 77, 10, 31),     // 31 November 1977
                new BirthDates.Row("C-311", 101, 1, 29),     // 29 February 2001
                new BirthDates.Row("C-312", 90, 4, 18)), rejected);

        assertThat(migrated).containsOnlyKeys("C-312");
        assertThat(rejected).extracting(BirthDates.Row::customer).containsExactly("C-310", "C-311");
    }
}
