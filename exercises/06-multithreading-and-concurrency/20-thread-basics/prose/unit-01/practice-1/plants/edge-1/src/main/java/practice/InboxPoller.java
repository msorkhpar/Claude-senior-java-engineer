package practice;

import java.util.function.Consumer;

/**
 * Polls the shared support inbox and hands each message on. It was written as a thread of its
 * own, and some deployments still run it that way with {@code new InboxPoller(...).start()}. The
 * service now also hands it to its background pool with {@code pool.execute(poller)}, and shuts
 * that pool down with {@code shutdownNow()} on every deploy.
 */
public class InboxPoller extends Thread {

    /** The inbox: {@code next()} returns the next message, or null when there is none yet. Never blocks. */
    public interface Inbox {
        String next();
    }

    private final Inbox inbox;
    private final Consumer<String> log;

    public InboxPoller(Inbox inbox, Consumer<String> log) {
        super("inbox-poller");
        this.inbox = inbox;
        this.log = log;
    }

    @Override
    public void run() {
        while (!stopRequested()) {
            String message = inbox.next();
            if (message == null) {
                Thread.onSpinWait();
                continue;
            }
            log.accept(workerName() + " handled " + message);
        }
        log.accept(workerName() + " stopped");
    }

    /** Whether whoever is running this poller has asked it to stop. */
    boolean stopRequested() {
        return isInterrupted();
    }

    /** The name of the thread doing the work, for the log lines. */
    String workerName() {
        return Thread.currentThread().getName();
    }
}
