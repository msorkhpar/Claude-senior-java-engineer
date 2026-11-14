package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
            List<Outbox.Message> batchList = new ArrayList<>();
            Outbox batch = new Outbox(batchList);
            batch.add(new Outbox.Message("ship-1", 400));
            batch.add(new Outbox.Message("ship-2", 250));
            batch.add(new Outbox.Message("ship-3", 260));
            batch.add(new Outbox.Message("ship-4", 900));
            System.out.println("batch dropped at 260: " + batch.dropExpired(260));
            System.out.println("batch list now: " + batch.pendingIds() + ", size " + batchList.size());
            List<Outbox.Message> liveList = new CopyOnWriteArrayList<>();
            Outbox live = new Outbox(liveList);
            live.add(new Outbox.Message("quote-7", 1_000));
            live.add(new Outbox.Message("quote-8", 30));
            live.add(new Outbox.Message("quote-9", 500));
            System.out.println("live dropped at 500: " + live.dropExpired(500));
            System.out.println("live list now: " + live.pendingIds() + ", size " + liveList.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
