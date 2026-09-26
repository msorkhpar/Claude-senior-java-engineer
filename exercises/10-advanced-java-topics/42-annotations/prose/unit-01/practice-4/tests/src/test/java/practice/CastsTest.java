package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CastsTest {

    private static List<Object> source(Object... items) {
        return new ArrayList<>(List.of(items));
    }

    @Test
    void aListOfStringsPassesThrough() {
        List<Object> src = source(new String("a"), new String("b"));

        assertThat(Casts.strings(src)).containsExactly("a", "b");
    }

    @Test
    void aWrongElementIsRefusedAtTheCall() {
        List<Object> src = source("a", 42);

        assertThatThrownBy(() -> Casts.strings(src)).isInstanceOf(IllegalArgumentException.class);
        List<Object> withNull = source("a");
        withNull.add(null);
        assertThatThrownBy(() -> Casts.strings(withNull)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyAListIsAccepted() {
        assertThatThrownBy(() -> Casts.strings("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Casts.strings(Set.of("a"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theResultIsACopy() {
        List<Object> src = source("a", "b");
        List<String> result = Casts.strings(src);

        src.set(0, "z");

        assertThat(result).containsExactly("a", "b");
    }
}
