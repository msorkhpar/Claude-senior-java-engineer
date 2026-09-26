package practice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class LineCounter {

    private LineCounter() {
    }

    /** Counts the non-blank lines, closing the stream before returning. */
    public static long countNonBlank(Stream<String> lines) {
        throw new UnsupportedOperationException("write countNonBlank(Stream)");
    }

    /** Counts the non-blank lines of a UTF-8 text file. */
    public static long countNonBlank(Path file) throws IOException {
        throw new UnsupportedOperationException("write countNonBlank(Path)");
    }
}
