package practice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            ErrorCounter counter = new ErrorCounter(line -> line.contains(" ERROR "));
            Map<String, ErrorCounter.Log> logs = new LinkedHashMap<>();
            logs.put("web-1.log", bytes("10:00 ERROR timeout\n10:01 INFO  ok\n10:02 ERROR timeout\n10:03 ERROR crash\n",
                    StandardCharsets.UTF_8));
            logs.put("depot-old.log", bytes("11:00 ERROR scale in Malmö\n11:05 INFO  café opened\n",
                    StandardCharsets.ISO_8859_1));
            logs.put("web-2.log", charset -> { throw new AccessDeniedException("/path/to/logs/web-2.log"); });
            ErrorCounter.Report report = counter.scan(logs);
            System.out.println("errors per log: " + report.errors());
            System.out.println("skipped: " + report.skipped().keySet());
            System.out.println("why web-2.log was skipped: " + report.skipped().get("web-2.log"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static ErrorCounter.Log bytes(String text, java.nio.charset.Charset written) {
        byte[] content = text.getBytes(written);
        return charset -> new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(content), charset.newDecoder()));
    }
}
