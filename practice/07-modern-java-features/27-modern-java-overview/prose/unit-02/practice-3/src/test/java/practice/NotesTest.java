package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class NotesTest {

    @TempDir
    Path dir;

    @Test
    void writesAndReadsANote() throws IOException {
        Path file = dir.resolve("notes.txt");
        Notes.append(file, "buy milk");
        assertThat(Notes.read(file)).containsExactly("buy milk");
        assertThat(Files.readString(file)).isEqualTo("buy milk\n");
    }

    @Test
    void appendKeepsEarlierNotes() throws IOException {
        Path file = dir.resolve("notes.txt");
        Notes.append(file, "buy milk");
        Notes.append(file, "call Bob");
        Notes.append(file, "caf\u00e9 at 9");
        assertThat(Notes.read(file)).containsExactly("buy milk", "call Bob", "caf\u00e9 at 9");
    }

    @Test
    void missingFileReadsAsNoNotes() throws IOException {
        assertThat(Notes.read(dir.resolve("absent.txt"))).isEmpty();
        assertThat(dir.resolve("absent.txt")).doesNotExist();
    }
}
