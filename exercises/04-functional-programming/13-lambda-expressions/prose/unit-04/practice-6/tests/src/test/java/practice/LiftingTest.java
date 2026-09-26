package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LiftingTest {

    @Test
    void keepsWhatParses() {
        assertThat(Lifting.parseAll(List.of("1", "abc", "3", "xyz", "5"))).containsExactly(1, 3, 5);
        assertThat(Lifting.parseAll(List.of("abc", "xyz"))).isEmpty();
        assertThat(Lifting.parseAll(List.of("+5", "-5", "99999999999", " 7", "8"))).containsExactly(5, -5, 8);

        CheckedFunction<String, Integer> parse = Integer::parseInt;
        assertThat(Lifting.lift(parse).apply("42")).contains(42);
    }

    @Test
    void aCheckedFailureIsEmpty() {
        CheckedFunction<String, String> read = name -> {
            throw new IOException("cannot read " + name);
        };

        assertThat(Lifting.lift(read).apply("notes.txt")).isEmpty();
    }

    @Test
    void aNullResultIsEmpty() {
        CheckedFunction<String, String> nothing = text -> null;

        assertThat(Lifting.lift(nothing).apply("anything")).isEmpty();
    }
}
