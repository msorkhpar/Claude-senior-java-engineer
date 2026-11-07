package practice;

/** Decides whether a job whose delivery failed is delivered again. */
public final class RetryPolicy {

    /**
     * A job as the broker delivers it. The producer shares this type, and every delivery is read
     * from the message into a new {@code Job} object.
     */
    public record Job(String id, String type, String payload) {
    }

    private final int maxDeliveries;

    /** @param maxDeliveries how many times one job is delivered at most */
    public RetryPolicy(int maxDeliveries) {
        if (maxDeliveries < 1) {
            throw new IllegalArgumentException("maxDeliveries must be at least 1");
        }
        this.maxDeliveries = maxDeliveries;
    }

    /**
     * TODO: called once each time a delivery of {@code job} fails. Returns whether the job is
     * delivered again.
     */
    public boolean failed(Job job) {
        throw new IllegalStateException("TODO: count this failed delivery");
    }

    /** TODO: how many deliveries of {@code job} have failed so far. */
    public int failures(Job job) {
        throw new IllegalStateException("TODO: report the failed deliveries");
    }
}
