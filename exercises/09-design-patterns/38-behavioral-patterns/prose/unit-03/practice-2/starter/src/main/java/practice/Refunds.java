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
            // TODO: Pay the refund out (payOut does that) and return its receipt. The same request
            //       can arrive more than once when a reply was lost on the way back.
            throw new IllegalStateException("TODO: handle");
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
            // TODO: Send the refund over the link and return the receipt from the reply. When no
            //       reply comes, try again, up to maxAttempts sends in all; after that, throw
            //       NoReplyException. Keys come from the keys supplier.
            throw new IllegalStateException("TODO: refund");
        }
    }
}
