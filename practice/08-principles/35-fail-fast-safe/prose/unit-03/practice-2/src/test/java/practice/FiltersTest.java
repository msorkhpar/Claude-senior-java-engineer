package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FiltersTest {

    @Test
    void keepsTheLongWords() {
        List<String> source = new ArrayList<>(List.of("a", "abc", "ab", "abcd"));
        assertThat(Filters.longWords(source, 3)).containsExactly("abc", "abcd");
        assertThat(Filters.longWords(new ArrayList<>(), 3)).isEmpty();
    }

    @Test
    void theSourceIsLeftUntouched() {
        List<String> source = new ArrayList<>(List.of("a", "abc", "ab", "abcd"));
        Filters.longWords(source, 3);
        assertThat(source).containsExactly("a", "abc", "ab", "abcd");
    }

    @Test
    void theResultIsNeverTheSource() {
        List<String> source = new ArrayList<>(List.of("abc", "abcd"));
        List<String> result = Filters.longWords(source, 3);
        assertThat(result).isNotSameAs(source).containsExactly("abc", "abcd");
        source.add("later");
        assertThat(result).containsExactly("abc", "abcd");
    }

    @Test
    void theResultCannotBeChanged() {
        List<String> result = Filters.longWords(new ArrayList<>(List.of("abc", "a")), 3);
        assertThatThrownBy(() -> result.add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.set(0, "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.remove(0)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void wordsAreKeptAsTheyAre() {
        List<String> source = new ArrayList<>(List.of("zzz", "abc", "zzz", " ab", "it's", "a"));
        assertThat(Filters.longWords(source, 3)).containsExactly("zzz", "abc", "zzz", " ab", "it's");
    }
}
