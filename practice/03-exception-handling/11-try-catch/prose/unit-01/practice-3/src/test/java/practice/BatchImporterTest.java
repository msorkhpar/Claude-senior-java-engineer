package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatchImporterTest {

    @Test
    void sumsValidRows() {
        List<String> log = new ArrayList<>();
        assertThat(BatchImporter.sum(List.of("1", "2", "3"), log)).isEqualTo(6);
        assertThat(BatchImporter.sum(List.of("-4", "10"), log)).isEqualTo(6);
        assertThat(BatchImporter.sum(List.of(), log)).isZero();
        assertThat(log).isEmpty();
    }

    @Test
    void aBadRowIsLoggedAndSkipped() {
        List<String> log = new ArrayList<>();
        assertThat(BatchImporter.sum(List.of("1", "x", "3", "4.5", " 7", "3000000000"), log)).isEqualTo(4);
        assertThat(log).containsExactly(
                "row 2: For input string: \"x\"",
                "row 4: For input string: \"4.5\"",
                "row 5: For input string: \" 7\"",
                "row 6: For input string: \"3000000000\"");
    }

    @Test
    void aNullRowIsLoggedNotACrash() {
        List<String> log = new ArrayList<>();
        assertThat(BatchImporter.sum(Arrays.asList("5", null, "2"), log)).isEqualTo(7);
        String message = null;
        try {
            Integer.parseInt(null);
        } catch (NumberFormatException e) {
            message = e.getMessage();
        }
        assertThat(log).containsExactly("row 2: " + message);
    }
}
