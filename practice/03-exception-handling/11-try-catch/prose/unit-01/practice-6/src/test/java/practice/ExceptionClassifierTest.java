package practice;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionClassifierTest {

    /** A nested exception type: its simple name has no enclosing prefix. */
    static final class CustomFailure extends RuntimeException {
        CustomFailure(String message) {
            super(message);
        }
    }

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
        assertThat(ExceptionClassifier.describe(() -> {
            throw new InvalidParameterException("bad key");
        })).isEqualTo("bad argument: bad key");
        assertThat(ExceptionClassifier.describe(() -> {
            throw new CustomFailure("custom");
        })).isEqualTo("unexpected CustomFailure: custom");
    }

    @Test
    void aNumberFormatExceptionIsNotJustABadArgument() {
        assertThat(ExceptionClassifier.describe(() -> Integer.parseInt("x")))
                .isEqualTo("not a number: For input string: \"x\"");
    }

    @Test
    void anErrorIsNotDescribed() {
        Error fatal = new Error("fatal");
        assertThatThrownBy(() -> ExceptionClassifier.describe(() -> {
            throw fatal;
        })).isSameAs(fatal);
    }
}
