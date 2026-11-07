package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShelfLabelTest {

    @Test
    void theJobCarriesTheCommandAndTheThreeFields() {
        String[] parts = ShelfLabel.job().split("\n", 2);

        assertThat(parts[0]).startsWith("^XA").endsWith("^XZ").contains("^FDAISLE B^FS");
        assertThat(parts[1].lines().map(String::strip).toList())
                .containsExactly("AISLE B / SHELF 07", "SKU 44-1093", "QTY 12");
    }

    @Test
    void everyBodyLineIsExactlyTwentyFourCharactersWide() {
        String body = ShelfLabel.body();

        assertThat(body).endsWith("\n");
        List<String> lines = body.lines().toList();
        assertThat(lines).hasSize(3);
        assertThat(lines).allSatisfy(line -> assertThat(line).hasSize(24));
        assertThat(lines.get(1)).isEqualTo("SKU 44-1093" + " ".repeat(13));
    }

    @Test
    void theSetupCommandArrivesExactlyAsWritten() {
        assertThat(ShelfLabel.setup())
                .isEqualTo("^XA^PW406^LL203^LH0,0^MD10^PR4^FO20,20^A0N,28,28^FDAISLE B^FS^XZ");
    }
}
