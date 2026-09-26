package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirstLineTest {

    @TempDir
    Path dir;

    @Test
    void readsTheFirstLine() throws IOException {
        Path two = Files.writeString(dir.resolve("two.txt"), "alpha\nbeta");
        Path one = Files.writeString(dir.resolve("one.txt"), "only");
        assertThat(FirstLine.read(two)).contains("alpha");
        assertThat(FirstLine.read(one)).contains("only");
    }

    @Test
    void anEmptyFileHasNoFirstLine() throws IOException {
        Path empty = Files.writeString(dir.resolve("empty.txt"), "");
        assertThat(FirstLine.read(empty)).isEmpty();
    }

    @Test
    void aMissingFileIsReportedNotHidden() {
        Path missing = dir.resolve("missing.txt");
        assertThatThrownBy(() -> FirstLine.read(missing)).isInstanceOf(IOException.class);
    }
}
