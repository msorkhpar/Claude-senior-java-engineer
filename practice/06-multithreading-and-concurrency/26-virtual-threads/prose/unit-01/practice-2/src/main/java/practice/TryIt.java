package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
            AtomicInteger built = new AtomicInteger();
            SchemaChecks checks = new SchemaChecks(() -> {
                built.incrementAndGet();
                return document -> {
                    if (document.startsWith("%PDF")) throw new IllegalArgumentException("not XML");
                    return document.contains("<signature>");
                };
            }, 3);
            ExecutorService uploads = Executors.newVirtualThreadPerTaskExecutor();
            try {
                List<Future<Boolean>> verdicts = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    String document = i == 7 ? "%PDF-1.4 scan" : i % 4 == 0 ? "<order n='" + i + "'/>" : "<order><signature>s" + i + "</signature></order>";
                    verdicts.add(uploads.submit(() -> checks.check(document)));
                }
                int valid = 0, invalid = 0, unreadable = 0;
                for (Future<Boolean> verdict : verdicts) {
                    try {
                        if (verdict.get(5, TimeUnit.SECONDS)) valid++; else invalid++;
                    } catch (ExecutionException e) {
                        if (!(e.getCause() instanceof IllegalArgumentException)) throw (RuntimeException) e.getCause();
                        unreadable++;
                    }
                }
                System.out.println("valid: " + valid + ", invalid: " + invalid + ", unreadable: " + unreadable);
                System.out.println("at most 3 checkers built for 20 uploads: " + (built.get() <= 3));
                System.out.println("an upload after them all: " + checks.check("<order><signature>late</signature></order>"));
            } finally {
                uploads.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
