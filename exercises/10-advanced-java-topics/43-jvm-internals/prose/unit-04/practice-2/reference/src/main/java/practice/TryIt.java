package practice;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import jdk.jfr.Recording;
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
            Path file = Files.createTempFile("tryit-alloc", ".jfr");
            try {
                try (Recording recording = new Recording()) {
                    recording.enable("jdk.ObjectAllocationSample").with("throttle", "300/s").withStackTrace();
                    recording.start();
                    Thread worker = new Thread(TryIt::work, "report-worker");
                    worker.start();
                    worker.join();
                    recording.stop();
                    recording.dump(file);
                }
                Map<String, Long> report = AllocationReport.bytesByMethod(file, "report-worker", "practice");
                long total = report.values().stream().mapToLong(Long::longValue).sum();
                report.forEach((method, bytes) -> System.out.println(method + ": " + bytes / 1_000_000 + " MB"));
                System.out.println("thread allocated: " + allocated / 1_000_000 + " MB");
                System.out.println("report within half of it: " + (total > allocated / 2 && total < allocated * 3 / 2));
                System.out.println("main thread in report: " + !AllocationReport.bytesByMethod(file, "main", "practice").isEmpty());
            } finally {
                Files.deleteIfExists(file);
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static long allocated;
    static long sink;

    static String buildInvoice(int lines) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines; i++) out.append("line-").append(i).append(';').append(i * 7 % 311).append('\n');
        return out.toString();
    }

    static String drawChart(int point) {
        return ("<rect x='" + point + "'/>").repeat(50);
    }

    static void work() {
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long before = threads.getCurrentThreadAllocatedBytes();
        long chars = 0;
        for (int round = 0; round < 3_000; round++) {
            chars += buildInvoice(2_000).length();
            for (int p = 0; p < 30; p++) chars += drawChart(round * 30 + p).length();
        }
        allocated = threads.getCurrentThreadAllocatedBytes() - before;
        sink = chars;
    }
}
