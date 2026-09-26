package practice;

/** A fixed stock of buffers, leased several at a time. */
public final class Leases {

    public Leases(int buffers) {
    }

    /** Waits until {@code n} buffers are free, then takes them all at once. */
    public void lease(int n) throws InterruptedException {
        throw new UnsupportedOperationException("write lease");
    }

    /** Takes {@code n} buffers only if all are free now; otherwise takes none. */
    public boolean tryLease(int n) {
        throw new UnsupportedOperationException("write tryLease");
    }

    /** Returns {@code n} buffers. */
    public void giveBack(int n) {
        throw new UnsupportedOperationException("write giveBack");
    }

    /** The number of free buffers. */
    public int available() {
        throw new UnsupportedOperationException("write available");
    }
}
