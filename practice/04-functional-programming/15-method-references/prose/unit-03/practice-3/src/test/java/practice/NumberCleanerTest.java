package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NumberCleanerTest {

    @Test
    void flattensParsesAndSortsDescending() {
        assertThat(NumberCleaner.clean(List.of(List.of("3", "1"), List.of("2", "3")))).containsExactly(3, 2, 1);
        assertThat(NumberCleaner.clean(List.of(List.of("-4", "10"), List.of()))).containsExactly(10, -4);
        assertThat(NumberCleaner.clean(List.of())).isEmpty();
    }

    @Test
    void paddedNumbersAreTrimmedBeforeParsing() {
        assertThat(NumberCleaner.clean(List.of(List.of("  42  ", " 7 "), List.of("  100  "))))
                .containsExactly(100, 42, 7);
    }

    @Test
    void nullsAndBlanksAreSkipped() {
        assertThat(NumberCleaner.clean(List.of(Arrays.asList("5", null, "", "   ", "6"))))
                .containsExactly(6, 5);
    }

    @Test
    void numbersThatReadTheSameAfterParsingAreOne() {
        assertThat(NumberCleaner.clean(List.of(List.of("7", "07"), List.of("8", " 7"))))
                .containsExactly(8, 7);
    }

    @Test
    void largeRepeatedNumbersAreOne() {
        assertThat(NumberCleaner.clean(List.of(List.of("1000", "5000"), List.of("1000"))))
                .containsExactly(5000, 1000);
    }
}
