package practice;

import java.util.Arrays;

/**
 * Collects latency samples between two metric flushes. It keeps them in a double[] (no boxing),
 * and replaces the array with a larger copy when it is full.
 */
public class SampleBuffer {

    private double[] samples;
    private int size;
    private long copied;                                  // elements copied by every growth so far

    /** A buffer with room for {@code initialCapacity} samples; 0 is allowed. */
    public SampleBuffer(int initialCapacity) {
        if (initialCapacity < 0) throw new IllegalArgumentException("capacity " + initialCapacity);
        samples = new double[initialCapacity];
    }

    public void add(double sample) {
        ensureRoom(1);
        samples[size++] = sample;
    }

    /** Appends a whole batch, for example the samples another node sent in one message. */
    public void addAll(double[] batch) {
        ensureRoom(batch.length);
        System.arraycopy(batch, 0, samples, size, batch.length);
        size += batch.length;
    }

    public double get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        return samples[index];
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return samples.length;
    }

    /** How many elements all growths so far have copied. */
    public long copied() {
        return copied;
    }

    private void ensureRoom(int more) {
        int needed = size + more;
        if (needed <= samples.length) return;
        samples = Arrays.copyOf(samples, newCapacity(samples.length, needed));
        copied += size;
    }

    /**
     * The capacity of the new array when the buffer, now {@code current} long, must hold
     * {@code needed} samples ({@code needed > current}).
     */
    static int newCapacity(int current, int needed) {
        return Math.max(needed, current + current / 2);
    }
}
