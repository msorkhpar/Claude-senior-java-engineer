package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeParserTest {

    private static Integer lengthOrFail(String s) throws IOException {
        if (s.isEmpty()) {
            throw new IOException("empty input");
        }
        if (s.equals("bad")) {
            throw new IllegalArgumentException("bad input");
        }
        return s.length();
    }

    @Test
    void parsesValidNumbers() {
        assertThat(SafeParser.parseAll(List.of("1", "2", "3"))).containsExactly(1, 2, 3);
        assertThat(SafeParser.parseAll(List.of("-7", "40", "+5"))).containsExactly(-7, 40, 5);
    }

    @Test
    void uncheckedAppliesTheFunction() {
        Function<String, Integer> length = SafeParser.unchecked(SafeParserTest::lengthOrFail);
        assertThat(length.apply("four")).isEqualTo(4);
        assertThat(length.apply("ab")).isEqualTo(2);

        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        Function<String, Integer> counted = SafeParser.unchecked(s -> calls.incrementAndGet() * 100 + s.length());
        assertThat(counted.apply("abc")).isEqualTo(103);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void invalidEntriesAreSkipped() {
        assertThat(SafeParser.parseAll(List.of("1", "abc", "3", "xyz", "5"))).containsExactly(1, 3, 5);
        assertThat(SafeParser.parseAll(List.of("abc", "xyz", "invalid"))).isEmpty();
        assertThat(SafeParser.parseAll(List.of(" 5", "99999999999", "6", "7 "))).containsExactly(6);
    }

    @Test
    void checkedExceptionKeepsItsCause() {
        Function<String, Integer> length = SafeParser.unchecked(SafeParserTest::lengthOrFail);
        assertThatThrownBy(() -> length.apply(""))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasCauseExactlyInstanceOf(IOException.class)
                .cause().hasMessage("empty input");
    }

    @Test
    void uncheckedExceptionsPassThrough() {
        Function<String, Integer> length = SafeParser.unchecked(SafeParserTest::lengthOrFail);
        assertThatThrownBy(() -> length.apply("bad"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("bad input");
    }

    @Test
    void parsingContinuesAfterABadEntry() {
        assertThat(SafeParser.parseAll(List.of("10", "x1", "20", "", "30"))).containsExactly(10, 20, 30);
    }
}
