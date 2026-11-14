package practice;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
            AtomicInteger inside = new AtomicInteger();
            AtomicInteger most = new AtomicInteger();
            AtomicBoolean onVirtual = new AtomicBoolean();
            ReportService.Renderer renderer = id -> {
                most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                if (Thread.currentThread().isVirtual()) onVirtual.set(true);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                inside.decrementAndGet();
                return ("pdf:" + id).getBytes(StandardCharsets.UTF_8);
            };
            ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
            try (ReportService service = new ReportService(renderer, 2)) {
                List<Future<byte[]>> replies = new ArrayList<>();
                for (String id : List.of("h1-revenue", "h1-costs", "payroll-may", "vat-q2", "headcount")) {
                    replies.add(requests.submit(() -> service.handle(id)));
                }
                for (Future<byte[]> reply : replies) System.out.println("got " + new String(finish(reply), StandardCharsets.UTF_8));
                System.out.println("rendered on a request's virtual thread: " + onVirtual.get());
                System.out.println("never more than 2 renders at once: " + (most.get() <= 2));
            } finally {
                requests.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Waits for a request's reply; passes on what it threw, so Run shows it. */
    static <T> T finish(Future<T> reply) throws Exception {
        try {
            return reply.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException stop) throw stop;
            throw e;
        }
    }
}
