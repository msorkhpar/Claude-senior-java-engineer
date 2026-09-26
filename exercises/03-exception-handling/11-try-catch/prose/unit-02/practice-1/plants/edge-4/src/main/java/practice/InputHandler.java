package practice;

import java.io.IOException;
import java.sql.SQLException;

public final class InputHandler {

    private InputHandler() {
    }

    /** Processes an input; may fail on I/O or on the database. */
    public interface Backend {
        String process(String input) throws IOException, SQLException;
    }

    /** Returns a one-line report of handling {@code input} with {@code backend}. */
    public static String handle(Backend backend, String input) {
        try {
            String result = backend.process(input);
            if (input == null) {
                return "Invalid input: Input is null";
            }
            if (input.isEmpty()) {
                return "Invalid input: Input is empty";
            }
            return "Input processed successfully: " + result;
        } catch (IOException | SQLException e) {
            return "Database or I/O error: " + e.getMessage();
        } catch (RuntimeException e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
}
