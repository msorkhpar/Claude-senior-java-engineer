package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ImporterTest {

    private final List<String> log = new ArrayList<>();
    private final Importer importer = new Importer(log);

    @Test
    void sumsGoodRecords() throws Exception {
        assertThat(importer.importAll(List.of("1", "2", "3"))).isEqualTo(6);
        assertThat(importer.importOrZero(List.of("1", "2", "3"))).isEqualTo(6);
        assertThat(log).isEmpty();
    }

    @Test
    void aBadRecordFailsTheImport() {
        ImportException e = catchThrowableOfType(
                () -> importer.importAll(List.of("1", "x", "3")), ImportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Bad record at line 2: x");
        assertThat(importer.importOrZero(List.of("1", "x", "3"))).isZero();
    }

    @Test
    void aHandledFailureIsLogged() {
        assertThat(importer.importOrZero(List.of("7", "seven"))).isZero();
        assertThat(log).contains("import failed: Bad record at line 2: seven");
    }

    @Test
    void aFailureIsLoggedExactlyOnce() {
        assertThat(importer.importOrZero(List.of("oops"))).isZero();
        assertThat(log).containsExactly("import failed: Bad record at line 1: oops");
    }

    @Test
    void theParseErrorIsTheCause() {
        ImportException e = catchThrowableOfType(
                () -> importer.importAll(List.of("1", "2.5")), ImportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getCause()).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void theFirstBadRecordStopsTheImport() {
        ImportException e = catchThrowableOfType(
                () -> importer.importAll(List.of("1", "x", "y")), ImportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Bad record at line 2: x");
    }

    @Test
    void aNumberBeyondIntIsABadRecord() {
        ImportException e = catchThrowableOfType(
                () -> importer.importAll(List.of("1", "4294967297")), ImportException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Bad record at line 2: 4294967297");
    }
}
