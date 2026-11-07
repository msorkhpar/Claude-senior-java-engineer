package practice;

/**
 * Lets batches that were processed in parallel commit in batch order. A worker that has finished
 * batch n calls awaitTurn(n), commits, then calls done(n), which gives the turn to batch n + 1.
 */
public class CommitSequencer {

    private final Object lock = new Object();
    private long next;

    /** The first batch to commit is firstBatch. */
    public CommitSequencer(long firstBatch) {
        this.next = firstBatch;
    }

    /** The batch whose turn it is. */
    public long next() {
        synchronized (lock) {
            return next;
        }
    }

    /** Returns when it is batch's turn to commit. */
    public void awaitTurn(long batch) throws InterruptedException {
        synchronized (lock) {
            if (next != batch) {
                lock.wait();
            }
        }
    }

    /** Called by the batch whose turn it is, after it committed: gives the turn to the next batch. */
    public void done(long batch) {
        synchronized (lock) {
            next = batch + 1;
            lock.notifyAll();
        }
    }
}
