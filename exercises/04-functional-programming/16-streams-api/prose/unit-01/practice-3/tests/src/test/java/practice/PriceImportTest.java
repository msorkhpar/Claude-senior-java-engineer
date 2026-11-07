package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;

class PriceImportTest {

    @Test
    void storesEveryPriceOfAGoodFile() throws Exception {
        PriceImport importer = new PriceImport();

        int stored = importer.importAll(List.of("TEA-0042;1299", "MUG-0007;850", "JAR-0300;45"));

        assertThat(stored).isEqualTo(3);
        assertThat(importer.prices()).containsExactly(
                entry("TEA-0042", 1299L), entry("MUG-0007", 850L), entry("JAR-0300", 45L));
    }

    @Test
    void skipsCommentsAndBlankLines() throws Exception {
        PriceImport importer = new PriceImport();

        int stored = importer.importAll(List.of(
                "# supplier 17, autumn list", "TEA-0042;1299", "", "# mugs", "MUG-0007;850"));

        assertThat(stored).isEqualTo(2);
        assertThat(importer.prices()).containsExactly(entry("TEA-0042", 1299L), entry("MUG-0007", 850L));
    }

    @Test
    void nothingAfterABadLineIsStored() {
        PriceImport importer = new PriceImport();

        catchThrowable(() -> importer.importAll(List.of(
                "TEA-0042;1299", "MUG-0007;eight", "JAR-0300;45", "PAN-0011;3999")));

        assertThat(importer.prices()).containsExactly(entry("TEA-0042", 1299L));
    }

    @Test
    void theCallerCanCatchTheBadLine() {
        PriceImport importer = new PriceImport();

        Throwable thrown = catchThrowable(() -> importer.importAll(List.of(
                "TEA-0042;1299", "JAR-0300", "PAN-0011;3999")));

        assertThat(thrown).isInstanceOf(PriceImport.BadLineException.class);
        assertThat(((PriceImport.BadLineException) thrown).lineNumber()).isEqualTo(2);
    }

    @Test
    void theBadLineIsNumberedAsInTheFile() {
        PriceImport importer = new PriceImport();

        Throwable thrown = catchThrowable(() -> importer.importAll(List.of(
                "# supplier 17", "", "TEA-0042;1299", "# jars", "JAR-0300;4,5")));

        assertThat(thrown).isInstanceOf(PriceImport.BadLineException.class);
        assertThat(((PriceImport.BadLineException) thrown).lineNumber()).isEqualTo(5);
    }
}
