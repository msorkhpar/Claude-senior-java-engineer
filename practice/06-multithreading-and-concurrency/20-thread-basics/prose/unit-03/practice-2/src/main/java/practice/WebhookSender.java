package practice;

import java.util.concurrent.BlockingQueue;

/**
 * Delivers the payloads in an outbox to a customer's webhook, one at a time. A failed delivery is
 * retried, with a growing pause between attempts. Runs on a thread of the notification pool,
 * which is shut down with shutdownNow() on every deploy.
 */
public class WebhookSender implements Runnable {

    /** One POST to the customer's endpoint; true when they accepted it. */
    public interface Endpoint {
        boolean post(String payload);
    }

    /** Waits the given time. In production this is Thread::sleep. */
    public interface Pause {
        void pause(long millis) throws InterruptedException;
    }

    private final BlockingQueue<String> outbox;
    private final Endpoint endpoint;
    private final Pause pause;
    private final int maxAttempts;

    public WebhookSender(BlockingQueue<String> outbox, Endpoint endpoint, Pause pause, int maxAttempts) {
        this.outbox = outbox;
        this.endpoint = endpoint;
        this.pause = pause;
        this.maxAttempts = maxAttempts;
    }

    public WebhookSender(BlockingQueue<String> outbox, Endpoint endpoint) {
        this(outbox, endpoint, Thread::sleep, 5);
    }

    /** Delivers payloads from the outbox, oldest first, for as long as the sender runs. */
    @Override
    public void run() {
        // TODO: Take each payload from the outbox (outbox.take() waits for one) and deliver it.
        //       The pool runs this method once and expects it to return when the pool is shut
        //       down, whether the sender is waiting for a payload or retrying one.
        throw new IllegalStateException("TODO: run");
    }

    /** Delivers one payload, retrying a failure. True when the endpoint accepted it. */
    boolean deliver(String payload) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (endpoint.post(payload)) return true;
            if (attempt == maxAttempts || !backOff(attempt)) return false;
        }
        return false;
    }

    /** Waits before the attempt after {@code attempt}. False when the sender must not retry. */
    boolean backOff(int attempt) {
        // TODO: Pause 200 ms times the attempt number, then let the retry go ahead. A deploy can
        //       arrive during the pause; after it, the customer's endpoint must not be hit again,
        //       and run() must still be able to tell that the sender was asked to stop.
        throw new IllegalStateException("TODO: backOff");
    }
}
