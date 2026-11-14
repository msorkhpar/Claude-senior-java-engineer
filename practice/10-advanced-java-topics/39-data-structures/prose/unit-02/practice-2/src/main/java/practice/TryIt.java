package practice;

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
            SampleBuffer buffer = new SampleBuffer(0);
            buffer.add(12.5);
            buffer.add(8.0);
            System.out.println("after 2 samples, capacity: " + buffer.capacity());
            buffer.addAll(new double[] {3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0});
            System.out.println("after a batch of 10, size: " + buffer.size() + ", capacity: " + buffer.capacity());
            System.out.println("sample 11: " + buffer.get(11));
            SampleBuffer busy = new SampleBuffer(8);
            int n = 300_000;
            for (int i = 0; i < n; i++) busy.add(i * 0.5);
            System.out.println("busy node, " + n + " samples, capacity: " + busy.capacity());
            System.out.println("elements copied: " + busy.copied() + " (" + busy.copied() / n + " per sample)");
            System.out.println("growth from 100 needing 101: " + SampleBuffer.newCapacity(100, 101));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
