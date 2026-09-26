package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class FileProcessorTest {

    private static final IOException NOT_FOUND = new IOException("File not found");

    private final FileProcessor processor = new FileProcessor(name -> {
        if (name.equals("nonexistent.txt")) {
            throw NOT_FOUND;
        }
        if (name.equals("empty.txt")) {
            return new String("");
        }
        if (name.equals("blank-line.txt")) {
            return new String("\n");
        }
        return "a\nb\nc";
    });

    @Test
    void countsTheLinesOfAReadableFile() throws Exception {
        assertThat(processor.countLines("notes.txt")).isEqualTo(3);
    }

    @Test
    void aFailedReadNamesTheFile() {
        FileProcessingException e = catchThrowableOfType(
                () -> processor.countLines("nonexistent.txt"), FileProcessingException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Error processing file: nonexistent.txt");
        assertThat(e.getFileName()).isEqualTo("nonexistent.txt");
    }

    @Test
    void theIoFailureIsKeptAsTheCause() {
        FileProcessingException e = catchThrowableOfType(
                () -> processor.countLines("nonexistent.txt"), FileProcessingException.class);
        assertThat(e).isNotNull();
        assertThat(e.getCause()).isSameAs(NOT_FOUND);
    }

    @Test
    void theContextFieldsAreFinal() {
        FileProcessingException e = catchThrowableOfType(
                () -> processor.countLines("nonexistent.txt"), FileProcessingException.class);
        assertThat(e).isNotNull();
        List<Field> fields = Arrays.stream(FileProcessingException.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
        assertThat(fields).isNotEmpty();
        assertThat(fields).allMatch(f -> Modifier.isFinal(f.getModifiers()));
    }

    @Test
    void emptyAndBlankFilesAreCountedExactly() throws Exception {
        assertThat(processor.countLines("empty.txt")).isZero();
        assertThat(processor.countLines("blank-line.txt")).isEqualTo(1);
    }
}
