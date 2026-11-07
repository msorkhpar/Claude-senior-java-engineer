package practice;

/**
 * Decides whether a canary build is promoted to every server. The metrics system reports how many
 * of the canary's requests succeeded; the gate turns that into an error rate and compares it with
 * the limit it was configured with.
 */
public class DeployGate {

    /** Two rates closer than this are the same rate: both come out of double arithmetic. */
    static final double TOLERANCE = 1e-9;

    public enum Verdict { PROMOTE, HOLD }

    private final double maxErrorRate;

    /** @param maxErrorRate the highest error rate, from 0.0 to 1.0, that is still promoted */
    public DeployGate(double maxErrorRate) {
        this.maxErrorRate = maxErrorRate;
    }

    /** Judges a canary that served {@code requests} requests, {@code succeeded} of them successfully. */
    public Verdict judge(long succeeded, long requests) {
        double errorRate = 1.0 - (double) succeeded / requests;
        return allows(errorRate) ? Verdict.PROMOTE : Verdict.HOLD;
    }

    /** Whether a canary with this error rate may be promoted. */
    boolean allows(double errorRate) {
        // TODO: Promote a canary whose error rate is at most maxErrorRate, and hold every other
        //       one. Hold anything the gate can't vouch for.
        throw new IllegalStateException("TODO: allows");
    }
}
