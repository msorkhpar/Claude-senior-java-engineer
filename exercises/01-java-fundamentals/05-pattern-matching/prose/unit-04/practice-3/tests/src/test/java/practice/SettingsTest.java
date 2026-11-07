package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class SettingsTest {

    private final Settings settings = new Settings();

    private static Map<String, Object> file(Object... keysAndValues) {
        Map<String, Object> parsed = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) parsed.put((String) keysAndValues[i], keysAndValues[i + 1]);
        return parsed;
    }

    @Test
    void showsEveryKindOfValue() {
        settings.load(file(
                "region", "eu-north",
                "pool.size", 24,
                "tls", true,
                "audit", false,
                "mirrors", List.of("a.example.com", "b.example.com", 3)));

        assertThat(settings.values()).containsExactly(
                entry("region", "eu-north"),
                entry("pool.size", "24"),
                entry("tls", "yes"),
                entry("audit", "no"),
                entry("mirrors", "a.example.com, b.example.com, 3"));
        assertThat(settings.unset()).isEmpty();
    }

    @Test
    void listsKeysWrittenWithoutAValue() {
        settings.load(file("region", "eu-north", "retries", null, "pool.size", 24, "proxy", null));

        assertThat(settings.unset()).containsExactly("retries", "proxy");
        assertThat(settings.values()).containsOnlyKeys("region", "pool.size");
    }

    @Test
    void refusesAValueOfAnotherType() {
        assertThatThrownBy(() -> settings.load(file("region", "eu-north", "ratio", 0.75)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ratio");
        assertThatThrownBy(() -> new Settings().load(file("limits", Map.of("cpu", 2))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limits");
    }
}
