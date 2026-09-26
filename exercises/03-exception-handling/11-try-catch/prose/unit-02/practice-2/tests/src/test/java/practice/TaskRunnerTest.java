package practice;

import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskRunnerTest {

    @Test
    void reportsTheResult() {
        assertThat(TaskRunner.run(() -> "42")).isEqualTo("done: 42");
    }

    @Test
    void reportsAnIoFailure() {
        assertThat(TaskRunner.run(() -> {
            throw new IOException("disk");
        })).isEqualTo("io: disk");
        assertThat(TaskRunner.run(() -> {
            throw new EOFException("eof");
        })).isEqualTo("io: eof");
        assertThat(TaskRunner.run(() -> {
            throw new IOException("No such file or directory");
        })).isEqualTo("io: No such file or directory");
    }

    @Test
    void reportsAnyOtherFailure() {
        assertThat(TaskRunner.run(() -> {
            throw new IllegalStateException("nope");
        })).isEqualTo("failed: nope");
        assertThat(TaskRunner.run(() -> {
            throw new Exception("checked");
        })).isEqualTo("failed: checked");
    }

    @Test
    void aMissingFileIsReportedAsMissing() {
        assertThat(TaskRunner.run(() -> {
            throw new FileNotFoundException("a.txt");
        })).isEqualTo("missing: a.txt");
    }

    @Test
    void anErrorIsNotCaught() {
        Error fatal = new Error("fatal");
        assertThatThrownBy(() -> TaskRunner.run(() -> {
            throw fatal;
        })).isSameAs(fatal);
        Throwable odd = new Throwable("neither an Exception nor an Error");
        assertThatThrownBy(() -> TaskRunner.run(() -> {
            throw TaskRunnerTest.<RuntimeException>sneaky(odd);
        })).isSameAs(odd);
    }

    /** Throws any throwable past the compiler's checks. */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
        throw (E) t;
    }
}
