package practice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            Path dir = Files.createTempDirectory("tryit-logs");
            try {
                Files.write(dir.resolve("api.log"), List.of("ERROR 502 from upstream", "INFO ok", "ERROR 502 from upstream"));
                Files.write(dir.resolve("auth.log"), List.of("INFO login", "ERROR token expired"));
                Files.write(dir.resolve("readme.md"), List.of("ERROR is just a word here"));
                Counting scanner = new Counting();
                System.out.println("logs: " + scanner.listLogs(dir).stream().map(Path::getFileName).toList());
                System.out.println("errors today: " + scanner.countErrors(dir));
                System.out.println("opened: " + scanner.opened);
                System.out.println("still open after the scan: " + scanner.open);
            } finally {
                try (Stream<Path> files = Files.list(dir)) {
                    for (Path file : files.toList()) Files.delete(file);
                }
                Files.delete(dir);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Notes what the scanner opens, and what is still open. */
    static class Counting extends LogScanner {
        final List<String> opened = new ArrayList<>();
        final List<String> open = new ArrayList<>();

        @Override
        Stream<Path> openDir(Path dir) throws IOException {
            return watch("dir", super.openDir(dir));
        }

        @Override
        Stream<String> openLog(Path log) throws IOException {
            return watch(log.getFileName().toString(), super.openLog(log));
        }

        private <T> Stream<T> watch(String name, Stream<T> stream) {
            opened.add(name);
            open.add(name);
            return stream.onClose(() -> open.remove(name));
        }
    }
}
