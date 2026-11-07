package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceFileTest {

    private final Locale before = Locale.getDefault();
    private final PriceFile file = new PriceFile();

    @BeforeEach
    void aServerInTheUs() {
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheDefault() {
        Locale.setDefault(before);
    }

    private static Map<String, Double> prices() {
        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("TEA-200", 3.4);
        prices.put("MUG-017", 12.0);
        prices.put("PEN-951", 0.85);
        return prices;
    }

    @Test
    void writesTwoDecimalsAndReadsThemBack() {
        String text = file.write(prices());

        assertThat(text).isEqualTo("TEA-200;3.40\nMUG-017;12.00\nPEN-951;0.85\n");
        assertThat(file.read(text)).containsExactlyEntriesOf(prices());
        assertThat(file.read("TEA-200;3.95\n\nPEN-951;1.10\n"))
                .containsExactly(Map.entry("TEA-200", 3.95), Map.entry("PEN-951", 1.10));
    }

    @Test
    void aServerWithAnotherDefaultLocaleWritesTheSameFile() {
        for (Locale locale : new Locale[]{Locale.FRANCE, Locale.forLanguageTag("de-AT")}) {
            Locale.setDefault(locale);

            String text = file.write(prices());

            assertThat(text).isEqualTo("TEA-200;3.40\nMUG-017;12.00\nPEN-951;0.85\n");
        }
    }

    @Test
    void aFieldThatIsNotAPriceIsRefused() {
        for (String field : new String[]{"NaN", "Infinity", "-Infinity", "4,20", ""}) {
            assertThatThrownBy(() -> file.read("TEA-200;" + field + "\n"))
                    .as(field)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
