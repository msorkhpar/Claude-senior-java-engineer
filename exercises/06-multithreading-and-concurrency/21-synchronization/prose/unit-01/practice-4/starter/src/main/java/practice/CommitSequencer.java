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
        // TODO: Return once next is batch, waiting until then without using the processor.
        //       Many workers wait here at once, each for its own batch.
        throw new IllegalStateException("TODO: awaitTurn");
    }

    /** Called by the batch whose turn it is, after it committed: gives the turn to the next batch. */
    public void done(long batch) {
        // TODO: Make batch + 1 the next batch, and let the worker waiting for it go ahead.
        throw new IllegalStateException("TODO: done");
    }
}
