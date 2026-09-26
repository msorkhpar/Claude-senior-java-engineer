package practice;

import org.junit.jupiter.api.Test;

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
    }
}
