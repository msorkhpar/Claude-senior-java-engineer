package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CleanNamesTest {

    @Test
    void trimsEveryName() {
        assertThat(CleanNames.clean(List.of("  hello  ", "world", " Ann"))).containsExactly("hello", "world", "Ann");
        assertThat(CleanNames.clean(List.of())).isEmpty();
    }

    @Test
    void nullsAreDropped() {
        assertThat(CleanNames.clean(Arrays.asList("  hello  ", null, "world", null))).containsExactly("hello", "world");
    }

    @Test
    void blankNamesAreDropped() {
        assertThat(CleanNames.clean(List.of("a", "   ", new String(""), " b "))).containsExactly("a", "b");
    }

    @Test
    void theResultCannotBeChanged() {
        List<String> cleaned = CleanNames.clean(List.of(" a ", "b"));

        assertThat(cleaned).containsExactly("a", "b");
        assertThatThrownBy(() -> cleaned.add("c")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theResultIsNotTheCallersList() {
        List<String> source = new ArrayList<>(List.of("ann", "bob"));

        List<String> cleaned = CleanNames.clean(source);
        source.add("cy");

        assertThat(cleaned).containsExactly("ann", "bob");
        assertThatThrownBy(() -> cleaned.add("dan")).isInstanceOf(UnsupportedOperationException.class);
    }
}
