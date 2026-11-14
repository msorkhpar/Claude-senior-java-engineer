package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
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
            List<String> rows = new ArrayList<>();
            for (int i = 0; i < 2_500; i++) rows.add("WH-" + (i % 4) + ";SKU-" + (40_000 + i) + ";on-hand=" + (i * 7 % 500));
            ByteArrayOutputStream connection = new ByteArrayOutputStream();
            new ReportResponder().send(rows, connection);
            int reportBytes = connection.size();
            connection.write("NEXT stock-delta 0007\n".getBytes(StandardCharsets.US_ASCII));
            byte[] report = java.util.Arrays.copyOf(connection.toByteArray(), reportBytes);
            System.out.println("report bytes on the wire: " + reportBytes);
            List<String> received;
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(report))) {
                received = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            }
            System.out.println("rows received: " + received.size() + " of " + rows.size());
            System.out.println("last row: " + received.get(received.size() - 1));
            System.out.println("connection after send: " + connection.size() + " bytes, next message written");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
