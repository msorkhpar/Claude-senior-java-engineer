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
        throw new UnsupportedOperationException("write handle");
    }
}
