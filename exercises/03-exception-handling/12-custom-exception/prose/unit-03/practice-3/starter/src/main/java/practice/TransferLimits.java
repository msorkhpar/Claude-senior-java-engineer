package practice;

/**
 * Checks a bank transfer against the per-transfer limits before it is sent. A rejected transfer's
 * exception message is written to the application log, which is shipped to the log aggregator,
 * and pasted into support tickets.
 */
public class TransferLimits {

    /** A transfer as the API received it. */
    public record TransferRequest(String id, String fromIban, String toIban, String apiToken, long amountCents) {
    }

    private final long minCents;
    private final long maxCents;

    /** Both limits are inclusive: a transfer of exactly minCents or maxCents is allowed. */
    public TransferLimits(long minCents, long maxCents) {
        this.minCents = minCents;
        this.maxCents = maxCents;
    }

    /** Throws IllegalArgumentException when the amount is outside the limits. */
    public void check(TransferRequest request) {
        if (request.amountCents() < minCents || request.amountCents() > maxCents) {
            throw new IllegalArgumentException(rejection(request));
        }
    }

    /** The message of the exception that rejects this request. */
    String rejection(TransferRequest request) {
        // TODO: Write the message the on-call engineer reads when a transfer is rejected. They
        //       must be able to tell from it alone what was wrong with this transfer and which
        //       transfer it was, and the log must stay safe to share.
        throw new IllegalStateException("TODO: rejection");
    }
}
