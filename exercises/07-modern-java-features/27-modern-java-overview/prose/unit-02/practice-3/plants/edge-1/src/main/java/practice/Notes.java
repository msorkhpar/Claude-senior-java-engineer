package practice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class Notes {

    private Notes() {
    }

    /** Adds the note as a new last line, creating the file when needed. */
    public static void append(Path file, String note) throws IOException {
        Files.writeString(file, note + "\n");
    }

    /** The notes in order; none when the file does not exist. */
    public static List<String> read(Path file) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        return Files.readString(file).lines().toList();
    }
}
