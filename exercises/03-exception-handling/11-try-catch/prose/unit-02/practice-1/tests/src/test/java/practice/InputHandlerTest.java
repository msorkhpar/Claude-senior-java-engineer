package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputHandlerTest {

    private static final InputHandler.Backend ECHO = input -> "processed " + input;

    private static final InputHandler.Backend UNTOUCHED = input -> {
        throw new AssertionError("invalid input must not reach the backend");
    };

    @Test
    void processesValidInput() {
        assertThat(InputHandler.handle(ECHO, "ValidInput"))
                .isEqualTo("Input processed successfully: processed ValidInput");
        assertThat(InputHandler.handle(ECHO, new String("  ")))
                .isEqualTo("Input processed successfully: processed   ");
        assertThat(InputHandler.handle(ECHO, new String(" x ")))
                .isEqualTo("Input processed successfully: processed  x ");
    }

    @Test
    void rejectsEmptyInput() {
        assertThat(InputHandler.handle(ECHO, new String(""))).isEqualTo("Invalid input: Input is empty");
    }

    @Test
    void reportsAnIoFailure() {
        InputHandler.Backend failing = input -> {
            throw new IOException("Simulated IOException");
        };
        assertThat(InputHandler.handle(failing, "x")).isEqualTo("Database or I/O error: Simulated IOException");
        InputHandler.Backend wrapped = input -> {
            throw new IOException("disk full", new IllegalStateException("inner"));
        };
        assertThat(InputHandler.handle(wrapped, "x")).isEqualTo("Database or I/O error: disk full");
    }

    @Test
    void aNullInputIsInvalidNotACrash() {
        assertThat(InputHandler.handle(UNTOUCHED, null)).isEqualTo("Invalid input: Input is null");
    }

    @Test
    void aSqlFailureIsADatabaseError() {
        InputHandler.Backend failing = input -> {
            throw new SQLException("Simulated SQLException");
        };
        assertThat(InputHandler.handle(failing, "x")).isEqualTo("Database or I/O error: Simulated SQLException");
    }

    @Test
    void anUncheckedFailureIsUnexpected() {
        InputHandler.Backend failing = input -> {
            throw new IllegalStateException("backend closed");
        };
        assertThat(InputHandler.handle(failing, "x")).isEqualTo("Unexpected error: backend closed");
    }

    @Test
    void invalidInputNeverReachesTheBackend() {
        List<String> seen = new ArrayList<>();
        InputHandler.Backend recording = input -> {
            seen.add(String.valueOf(input));
            return "processed " + input;
        };
        assertThat(InputHandler.handle(recording, new String(""))).isEqualTo("Invalid input: Input is empty");
        assertThat(InputHandler.handle(recording, null)).isEqualTo("Invalid input: Input is null");
        assertThat(seen).isEmpty();
    }

    @Test
    void anErrorFromTheBackendIsNotReported() {
        Error fatal = new Error("backend crashed");
        InputHandler.Backend crashing = input -> {
            throw fatal;
        };
        assertThatThrownBy(() -> InputHandler.handle(crashing, "x")).isSameAs(fatal);
    }
}
