package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/** Refunds, sent by the shop's back office to the refund service over a link that can lose replies. */
public class Refunds {

    /** One refund request. The key names the request; the shop creates one per refund. */
    public record RefundCommand(String key, String orderId, long cents) {}

    /** A refund the service paid out. */
    public record Payout(String receipt, String orderId, long cents) {}

    /** The link to the refund service. A TimeoutException means no reply came in time. */
    public interface Link {
        String send(RefundCommand command) throws TimeoutException;
    }

    /** Thrown when no attempt got a reply. */
    public static class NoReplyException extends RuntimeException {
        public NoReplyException(String message) {
            super(message);
        }
    }

    /** The refund service: pays refunds out of the shop's account. */
    public static class RefundService {
        private final List<Payout> payouts = new ArrayList<>();
        private final Map<String, String> receipts = new ConcurrentHashMap<>();

        /** Every refund paid out, oldest first. */
        public synchronized List<Payout> payouts() {
            return List.copyOf(payouts);
        }

        /** Pays the refund and returns its receipt. */
        private synchronized String payOut(RefundCommand command) {
            String receipt = "rf-" + (payouts.size() + 1);
            payouts.add(new Payout(receipt, command.orderId(), command.cents()));
            return receipt;
        }

        /** Handles one request from the link and returns the receipt the reply carries. */
        public String handle(RefundCommand command) {
            return receipts.computeIfAbsent(command.orderId() + "/" + command.cents(), k -> payOut(command));
        }
    }

    /** The back office's side: asks for a refund and retries when the reply doesn't come. */
    public static class RefundClient {
        private final Link link;
        private final Supplier<String> keys;
        private final int maxAttempts;

        public RefundClient(Link link, Supplier<String> keys, int maxAttempts) {
            this.link = link;
            this.keys = keys;
            this.maxAttempts = maxAttempts;
        }

        /** Refunds the amount on the order and returns the receipt. */
        public String refund(String orderId, long cents) {
            RefundCommand command = new RefundCommand(keys.get(), orderId, cents);
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    return link.send(command);
                } catch (TimeoutException e) {
                    // no reply: the refund may or may not have been paid, so ask again
                }
            }
            throw new NoReplyException("no reply for a refund on " + orderId);
        }
    }
}
