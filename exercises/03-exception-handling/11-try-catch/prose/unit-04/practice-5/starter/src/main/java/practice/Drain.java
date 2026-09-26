package practice;

import java.io.IOException;
import java.util.List;

public final class Drain {

    private Drain() {
    }

    /** A source of lines; readLine() returns null at the end. */
    public interface LineSource extends AutoCloseable {
        String readLine() throws IOException;

        @Override
        void close() throws IOException;
    }

    /** Reads every line of an already-open {@code source}, then closes it. */
    public static List<String> readAll(LineSource source) throws IOException {
        throw new UnsupportedOperationException("write readAll");
    }
}
