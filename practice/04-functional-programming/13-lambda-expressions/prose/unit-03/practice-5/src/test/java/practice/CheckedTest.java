package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckedTest {

    @Test
    void appliesTheFunction() {
        CheckedFunction<String, Integer> parse = Integer::parseInt;

        assertThat(Checked.unchecked(parse).apply("4200")).isEqualTo(4200);
        assertThat(Checked.mapAll(List.of("1000", "2000"), parse)).containsExactly(1000, 2000);
        assertThat(Checked.mapAll(List.<String>of(), parse)).isEmpty();
    }

    @Test
    void failureKeepsTheOriginalCause() {
        IOException disk = new IOException("disk unavailable");
        CheckedFunction<String, String> read = name -> {
            throw disk;
        };

        assertThatThrownBy(() -> Checked.unchecked(read).apply("notes.txt"))
                .isInstanceOf(ApplyFailedException.class)
                .hasMessage("failed on notes.txt")
                .cause().isSameAs(disk);
    }

    @Test
    void uncheckedFailuresAreWrappedToo() {
        CheckedFunction<String, Integer> parse = Integer::parseInt;

        assertThatThrownBy(() -> Checked.unchecked(parse).apply("abc"))
                .isInstanceOf(ApplyFailedException.class)
                .hasMessage("failed on abc")
                .cause().isInstanceOf(NumberFormatException.class);
    }

    @Test
    void mapAllStopsAtTheFirstFailure() {
        AtomicInteger calls = new AtomicInteger();
        CheckedFunction<String, Integer> countedParse = text -> {
            calls.incrementAndGet();
            return Integer.parseInt(text);
        };

        assertThatThrownBy(() -> Checked.mapAll(List.of("1", "abc", "3", "xyz"), countedParse))
                .isInstanceOf(ApplyFailedException.class)
                .hasMessage("failed on abc");
        assertThat(calls).hasValue(2);
    }
}
