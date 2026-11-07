package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportTest {

    private final CsvExport export = new CsvExport();

    @Test
    void writesStringsNumbersAndFlagsAsText() {
        assertThat(export.line(List.of("u-1182", 3, 41.25, false, 'Q')))
                .isEqualTo("u-1182,3,41.25,false,Q");
    }

    @Test
    void writesATokenAsTheCharactersItHolds() {
        char[] token = "tk-90f".toCharArray();

        assertThat(export.line(List.of("u-77", token, 2))).isEqualTo("u-77,tk-90f,2");
    }

    @Test
    void writesAnEmptyCellAsNothing() {
        assertThat(export.line(Arrays.asList("u-5", null, 0))).isEqualTo("u-5,,0");
    }
}
