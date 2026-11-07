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
        return samples.clone();
    }

    @Override
    public int[] samples() {
        return samples.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Batch other
                && device.equals(other.device)
                && Arrays.equals(samples, other.samples);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(device, samples);
    }

    @Override
    public String toString() {
        return "Batch[device=" + device + ", samples=" + Arrays.toString(samples) + "]";
    }
}
