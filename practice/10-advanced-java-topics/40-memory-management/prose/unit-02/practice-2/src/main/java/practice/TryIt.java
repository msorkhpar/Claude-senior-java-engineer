package practice;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            Cleaner cleaner = Cleaner.create(r -> {
                Thread t = new Thread(r, "tryit-cleaner");
                t.setDaemon(true);
                return t;
            });
            Counting allocator = new Counting();
            try (NativeBuffer thumbnail = new NativeBuffer(cleaner, allocator, 4096)) {
                System.out.println("allocated at: " + Long.toHexString(thumbnail.address()));
            }
            System.out.println("frees after try-with-resources: " + allocator.freed);
            NativeBuffer twice = new NativeBuffer(cleaner, allocator, 1024);
            twice.close();
            twice.close();
            System.out.println("frees after closing another buffer twice: " + allocator.freed);
            long forgotten = forget(cleaner, allocator);
            for (int i = 0; i < 40 && !allocator.freed.contains(forgotten); i++) {
                System.gc();
                Thread.sleep(50);
            }
            System.out.println("forgotten buffer " + Long.toHexString(forgotten) + " freed: " + allocator.freed.contains(forgotten));
            System.out.println("total frees: " + allocator.freed.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class Counting implements NativeBuffer.Allocator {
        private long next = 0x7000_0000L;
        final List<Long> freed = Collections.synchronizedList(new ArrayList<>());
        @Override public synchronized long allocate(int bytes) { long a = next; next += bytes + 4096L; return a; }
        @Override public void free(long address) { freed.add(address); }
    }

    private static long forget(Cleaner cleaner, Counting allocator) {
        return new NativeBuffer(cleaner, allocator, 65_536).address();
    }
}
