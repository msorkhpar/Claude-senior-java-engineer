package practice;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UncheckedTest {

    private static IOException lastThrown;

    static String load(String key) throws IOException {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }
        if (key.startsWith("missing")) {
            lastThrown = new FileNotFoundException(key);
            throw lastThrown;
        }
        return "value of " + key;
    }

    @Test
    void wrapsAMethodThatDeclaresIOException() {
        assertThat(Unchecked.function(UncheckedTest::load).apply("a")).isEqualTo("value of a");
        assertThat(Unchecked.mapAll(List.of("a", "b"), UncheckedTest::load))
                .containsExactly("value of a", "value of b");
    }

    @Test
    void anIOExceptionBecomesUncheckedWithItsCause() {
        assertThatThrownBy(() -> Unchecked.function(UncheckedTest::load).apply("missing.txt"))
                .isExactlyInstanceOf(UncheckedIOException.class)
                .cause()
                .isExactlyInstanceOf(FileNotFoundException.class)
                .hasMessage("missing.txt");
    }

    @Test
    void otherRuntimeExceptionsPassThroughUnchanged() {
        assertThatThrownBy(() -> Unchecked.mapAll(List.of("a", ""), UncheckedTest::load))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("empty key");
    }

    @Test
    void theCauseIsTheOriginalException() {
        assertThatThrownBy(() -> Unchecked.function(UncheckedTest::load).apply("missing-config"))
                .isInstanceOf(UncheckedIOException.class)
                .cause()
                .isSameAs(lastThrown);
    }
}
