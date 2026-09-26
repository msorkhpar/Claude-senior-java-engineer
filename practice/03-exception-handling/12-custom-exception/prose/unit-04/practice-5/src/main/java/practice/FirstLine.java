package practice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class FirstLine {

    private FirstLine() {
    }

    /** The file's first line, or empty for an empty file; a file that cannot be read throws IOException. */
    public static Optional<String> read(Path path) throws IOException {
        throw new UnsupportedOperationException("write read");
    }
}
