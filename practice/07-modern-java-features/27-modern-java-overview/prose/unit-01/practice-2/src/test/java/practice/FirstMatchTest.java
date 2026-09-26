package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class FirstMatchTest {

    @Test
    void findsTheFirstCleanedMatch() {
        List<String> raw = List.of(" a ", " bb ", " ccc ", " dd ");
        assertThat(FirstMatch.first(raw, String::strip, s -> s.length() == 2)).contains("bb");
        assertThat(FirstMatch.first(raw, String::strip, s -> s.length() == 3)).contains("ccc");
    }

    @Test
    void noMatchIsAnEmptyOptional() {
        assertThat(FirstMatch.first(List.of(" a ", " b "), String::strip, s -> s.length() == 2)).isEmpty();
        assertThat(FirstMatch.first(List.of(), String::strip, s -> true)).isEmpty();
    }

    @Test
    void stopsCleaningAtTheFirstMatch() {
        AtomicInteger calls = new AtomicInteger();
        Function<String, String> counted = s -> {
            calls.incrementAndGet();
            return s.strip();
        };
        List<String> raw = List.of(" a ", " bb ", " ccc ", " dd ", " e ");
        assertThat(FirstMatch.first(raw, counted, s -> s.length() == 2)).contains("bb");
        assertThat(calls.get()).as("clean calls").isEqualTo(2);
    }

    @Test
    void nullsAreSkippedUncleaned() {
        List<String> raw = new ArrayList<>(Arrays.asList(null, " x ", null));
        assertThat(FirstMatch.first(raw, s -> s.strip(), s -> !s.isEmpty())).contains("x");
        assertThat(FirstMatch.first(raw, String::strip, s -> true)).contains("x");
    }
}
