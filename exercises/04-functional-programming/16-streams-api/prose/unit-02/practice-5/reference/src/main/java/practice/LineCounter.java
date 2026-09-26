package practice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class LineCounter {

    private LineCounter() {
    }

    /** Counts the non-blank lines, closing the stream before returning. */
    public static long countNonBlank(Stream<String> lines) {
        try (lines) {
            return lines.filter(line -> !line.isBlank()).count();
        }
    }

    /** Counts the non-blank lines of a UTF-8 text file. */
    public static long countNonBlank(Path file) throws IOException {
        return countNonBlank(Files.lines(file));
    }
}
