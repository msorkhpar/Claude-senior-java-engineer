package practice;

import java.io.IOException;
import java.sql.SQLException;

public final class InputHandler {

    private InputHandler() {
    }

    public interface Backend {
        String process(String input) throws IOException, SQLException;
    }

    public static String handle(Backend backend, String input) {
        if (input == null) {
            return "Invalid input: Input is null";
        }
        if (input.isEmpty()) {
            return "Invalid input: Input is empty";
        }
        try {
            return "Input processed successfully: " + backend.process(input);
        } catch (IOException | SQLException e) {
            return "Database or I/O error: " + e.getMessage();
        } catch (Throwable e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
}
