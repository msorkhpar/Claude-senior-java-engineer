package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ListingTest {

    @Test
    void numbersEachLine() {
        assertThat(Listing.number("a\nb")).containsExactly("1: a", "2: b");
        assertThat(Listing.number("a\n\nb")).containsExactly("1: a", "2: ", "3: b");
        assertThat(Listing.number("only")).containsExactly("1: only");
    }

    @Test
    void everyLineTerminatorSplits() {
        assertThat(Listing.number("a\r\nb\rc")).containsExactly("1: a", "2: b", "3: c");
    }

    @Test
    void aFinalTerminatorAddsNoLine() {
        assertThat(Listing.number("a\nb\n")).containsExactly("1: a", "2: b");
        assertThat(Listing.number("a\n\n")).containsExactly("1: a", "2: ");
        assertThat(Listing.number("\n")).containsExactly("1: ");
    }

    @Test
    void emptyTextHasNoLines() {
        assertThat(Listing.number("")).isEmpty();
    }

    @Test
    void numbersAreRightAligned() {
        String text = IntStream.rangeClosed(1, 10).mapToObj(i -> "l" + i).collect(Collectors.joining("\n"));
        List<String> numbered = Listing.number(text);
        assertThat(numbered).hasSize(10);
        assertThat(numbered.get(0)).isEqualTo(" 1: l1");
        assertThat(numbered.get(8)).isEqualTo(" 9: l9");
        assertThat(numbered.get(9)).isEqualTo("10: l10");
        String hundred = IntStream.rangeClosed(1, 100).mapToObj(i -> "l" + i).collect(Collectors.joining("\n"));
        List<String> wide = Listing.number(hundred);
        assertThat(wide.get(0)).isEqualTo("  1: l1");
        assertThat(wide.get(9)).isEqualTo(" 10: l10");
        assertThat(wide.get(99)).isEqualTo("100: l100");
    }
}
