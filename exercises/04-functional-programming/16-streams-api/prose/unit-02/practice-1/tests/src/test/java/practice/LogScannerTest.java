package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogScannerTest {

    @TempDir
    Path dir;

    /** Watches every stream the scanner opens and whether it was closed. */
    static class Watched extends LogScanner {
        final List<String> opened = Collections.synchronizedList(new ArrayList<>());
        final List<String> closed = Collections.synchronizedList(new ArrayList<>());
        String failing;   // a log whose reading fails after its first line

        @Override
        Stream<Path> openDir(Path dir) throws IOException {
            opened.add("dir");
            return super.openDir(dir).onClose(() -> closed.add("dir"));
        }

        @Override
        Stream<String> openLog(Path log) throws IOException {
            String name = log.getFileName().toString();
            opened.add(name);
            Stream<String> lines = name.equals(failing)
                    ? Stream.iterate(0, i -> i < 3, i -> i + 1).map(i -> {
                        if (i == 1) throw new UncheckedIOException(new IOException("device went away"));
                        return "ERROR line " + i;
                    })
                    : super.openLog(log);
            return lines.onClose(() -> closed.add(name));
        }
    }

    private void write(String name, String... lines) throws IOException {
        Files.write(dir.resolve(name), List.of(lines));
    }

    private void someLogs() throws IOException {
        write("payments-2.log", "INFO started", "ERROR card declined", "ERROR timeout to bank", "INFO stopped");
        write("payments-1.log", "INFO started", "WARN slow", "INFO stopped");
        write("search.log", "ERROR index missing", "INFO rebuilt index");
        write("notes.txt", "ERROR this file is not a log");
    }

    @Test
    void countsTheErrorLinesOfEveryLog() throws IOException {
        someLogs();

        assertThat(new Watched().countErrors(dir)).isEqualTo(3);
    }

    @Test
    void listsOnlyTheLogsInNameOrder() throws IOException {
        someLogs();

        assertThat(new Watched().listLogs(dir)).extracting(p -> p.getFileName().toString())
                .containsExactly("payments-1.log", "payments-2.log", "search.log");
    }

    @Test
    void everyLogItReadsIsClosedAgain() throws IOException {
        someLogs();
        Watched scanner = new Watched();

        scanner.countErrors(dir);

        List<String> logsOpened = scanner.opened.stream().filter(n -> !n.equals("dir")).toList();
        assertThat(logsOpened).hasSize(3);
        assertThat(scanner.closed).containsAll(logsOpened);
    }

    @Test
    void theDirectoryListingIsClosedAgain() throws IOException {
        someLogs();
        Watched scanner = new Watched();

        scanner.countErrors(dir);

        assertThat(scanner.opened).contains("dir");
        assertThat(scanner.closed).contains("dir");
    }

    @Test
    void aLogWhoseReadingFailsIsClosedToo() throws IOException {
        someLogs();
        Watched scanner = new Watched();
        scanner.failing = "payments-2.log";

        assertThatThrownBy(() -> scanner.countErrors(dir)).isInstanceOf(UncheckedIOException.class);

        assertThat(scanner.opened).contains("payments-2.log");
        assertThat(scanner.closed).contains("payments-2.log");
    }
}
