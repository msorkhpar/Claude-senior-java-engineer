package practice;

import java.nio.file.Files;
import java.nio.file.Path;
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
            Path disk = Files.createTempDirectory("tryit-store").toRealPath();
            try {
                Path store = Files.createDirectories(disk.resolve("files/R-31"));
                Files.writeString(store.resolve("invoice.txt"), "invoice of R-31");
                Files.writeString(disk.resolve("secrets.env"), "token=placeholder");
                Files.createDirectories(disk.resolve("files-backup"));
                Files.writeString(disk.resolve("files-backup/old.txt"), "old copy");
                Files.createSymbolicLink(disk.resolve("files/shortcut"), Path.of(".."));
                Files.createSymbolicLink(disk.resolve("live"), Path.of("files"));
                AttachmentStore attachments = new AttachmentStore(disk.resolve("live"));
                for (String name : new String[] {"R-31/invoice.txt", "R-31/../R-31/invoice.txt", "../secrets.env",
                        "../files-backup/old.txt", "shortcut/secrets.env"}) {
                    try {
                        System.out.println(name + " -> " + attachments.read(name));
                    } catch (SecurityException refused) {
                        System.out.println(name + " -> refused");
                    }
                }
            } finally {
                try (var files = Files.walk(disk)) {
                    files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
