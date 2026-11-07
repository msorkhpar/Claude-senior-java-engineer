package practice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Counts the lines that contain "ERROR" in every log of a directory. The support team runs it
 * every few minutes from a long-lived service.
 */
public class LogScanner {

    /** Opens a directory listing. Always use this, never Files.list directly. */
    Stream<Path> openDir(Path dir) throws IOException {
        return Files.list(dir);
    }

    /** Opens the lines of one log. Always use this, never Files.lines directly. */
    Stream<String> openLog(Path log) throws IOException {
        return Files.lines(log);
    }

    /** How many lines of all the logs in dir contain "ERROR". */
    public long countErrors(Path dir) throws IOException {
        long total = 0;
        for (Path log : listLogs(dir)) {
            total += errorsIn(log);
        }
        return total;
    }

    /** The files in dir whose names end in ".log", sorted by name. */
    List<Path> listLogs(Path dir) throws IOException {
        // TODO: List the directory through openDir and keep the ".log" files, sorted by name.
        //       This runs every few minutes for as long as the service is up.
        throw new IllegalStateException("TODO: listLogs");
    }

    /** How many lines of this log contain "ERROR". */
    long errorsIn(Path log) throws IOException {
        // TODO: Count this log's lines that contain "ERROR", reading it through openLog.
        //       A log can fail halfway through reading; that failure goes to the caller.
        throw new IllegalStateException("TODO: errorsIn");
    }
}
