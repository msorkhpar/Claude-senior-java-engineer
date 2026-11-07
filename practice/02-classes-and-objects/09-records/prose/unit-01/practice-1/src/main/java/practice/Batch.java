package practice;

import java.util.Arrays;

/**
 * One batch of temperature samples, as a sensor sends it to the gateway. The gateway keeps the
 * batches it has stored in a HashSet and drops a batch it has already seen: sensors resend a
 * batch whenever an acknowledgement is lost.
 */
public record Batch(String device, int[] samples) {

    public Batch {
        samples = keep(samples);
    }

    /** What the batch stores, given the array the sensor's driver passed in. */
    static int[] keep(int[] samples) {
        // TODO: The driver reads every batch into the same buffer and passes that buffer in.
        //       Decide what this batch stores so that the next read can't change it.
        throw new IllegalStateException("TODO: keep");
    }

    @Override
    public int[] samples() {
        // TODO: Whoever reads the samples of a stored batch must not be able to change the batch.
        throw new IllegalStateException("TODO: samples");
    }

    @Override
    public boolean equals(Object o) {
        // TODO: A resent batch is the same device with the same samples, in the same order.
        throw new IllegalStateException("TODO: equals");
    }

    @Override
    public int hashCode() {
        // TODO: Must agree with equals, so the gateway's HashSet finds a resent batch.
        throw new IllegalStateException("TODO: hashCode");
    }

    @Override
    public String toString() {
        return "Batch[device=" + device + ", samples=" + Arrays.toString(samples) + "]";
    }
}
