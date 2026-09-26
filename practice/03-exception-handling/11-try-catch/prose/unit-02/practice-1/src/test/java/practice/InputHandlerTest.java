package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class InputHandlerTest {

    private static final InputHandler.Backend ECHO = input -> "processed " + input;

    private static final InputHandler.Backend UNTOUCHED = input -> {
        throw new AssertionError("invalid input must not reach the backend");
    };

    @Test
    void processesValidInput() {
        assertThat(InputHandler.handle(ECHO, "ValidInput"))
                .isEqualTo("Input processed successfully: processed ValidInput");
    }

    @Test
    void rejectsEmptyInput() {
        assertThat(InputHandler.handle(UNTOUCHED, new String(""))).isEqualTo("Invalid input: Input is empty");
    }

    @Test
    void reportsAnIoFailure() {
        InputHandler.Backend failing = input -> {
            throw new IOException("Simulated IOException");
        };
        assertThat(InputHandler.handle(failing, "x")).isEqualTo("Database or I/O error: Simulated IOException");
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
}
