package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassifierTest {

    @Test
    void describesEachKind() {
        assertThat(ExceptionClassifier.describe(() -> { })).isEqualTo("ok");
        assertThat(ExceptionClassifier.describe(() -> {
            throw new IllegalArgumentException("negative");
        })).isEqualTo("bad argument: negative");
        assertThat(ExceptionClassifier.describe(() -> {
            int zero = 0;
            System.out.println(1 / zero);
        })).isEqualTo("arithmetic: / by zero");
        assertThat(ExceptionClassifier.describe(() -> {
            throw new IllegalStateException("closed");
        })).isEqualTo("unexpected IllegalStateException: closed");
    }

    @Test
    void aNumberFormatExceptionIsNotJustABadArgument() {
        assertThat(ExceptionClassifier.describe(() -> Integer.parseInt("x")))
                .isEqualTo("not a number: For input string: \"x\"");
    }
}
