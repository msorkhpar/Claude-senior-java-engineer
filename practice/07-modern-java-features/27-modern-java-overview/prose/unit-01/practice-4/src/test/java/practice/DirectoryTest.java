package practice;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryTest {

    private static Directory directory() {
        return new Directory(Map.of(1, "Alice", 2, "Bob", 3, "Carol", 4, "Dave", 5, "Linda"), Map.of("Alice", "Paris", "Bob", "Lyon"));
    }

    @Test
    void looksUpNameCityAndBadge() {
        Directory directory = directory();
        assertThat(directory.name(1)).contains("Alice");
        assertThat(directory.city(1)).contains("Paris");
        assertThat(directory.badge(1)).isEqualTo("ALICE");
        assertThat(directory.city(2)).contains("Lyon");
    }

    @Test
    void unknownUserIsEmpty() {
        Directory directory = directory();
        assertThat(directory.name(9)).isEmpty();
        assertThat(directory.city(9)).isEmpty();
    }

    @Test
    void userWithoutCityIsEmpty() {
        assertThat(directory().city(3)).isEmpty();
    }

    @Test
    void badgeIgnoresTheDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(directory().badge(5)).isEqualTo("LINDA");
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void shortOrMissingNameGetsGuestBadge() {
        Directory directory = directory();
        assertThat(directory.badge(2)).isEqualTo("GUEST");
        assertThat(directory.badge(9)).isEqualTo("GUEST");
        assertThat(directory.badge(3)).isEqualTo("CAROL");
        assertThat(directory.badge(4)).isEqualTo("DAVE");
    }
}
