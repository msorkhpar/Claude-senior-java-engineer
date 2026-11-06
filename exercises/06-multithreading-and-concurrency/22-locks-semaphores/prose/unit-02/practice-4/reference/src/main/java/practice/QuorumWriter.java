package practice;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Writes records to a set of replicas, one write at a time. A write succeeds once a quorum of
 * replicas has acknowledged it; the others catch up later.
 */
public class QuorumWriter {

    /**
     * One replica. It stores the record and calls {@code ack.run()} once it has, from whatever
     * thread it likes: before store() returns, later, or never if it is down.
     */
    public interface Replica {
        void store(long version, String record, Runnable ack);
    }

    private final List<Replica> replicas;
    private final int quorum;
    private final long timeoutMillis;
    private long version;

    public QuorumWriter(List<Replica> replicas, int quorum, long timeoutMillis) {
        this.replicas = List.copyOf(replicas);
        this.quorum = quorum;
        this.timeoutMillis = timeoutMillis;
    }

    /** Writes one record. True once a quorum acknowledged it, false if none came in time. */
    public synchronized boolean write(String record) throws InterruptedException {
        long v = ++version;
        Semaphore acks = acksForNextWrite();
        for (Replica replica : replicas) replica.store(v, record, acks::release);
        return awaitQuorum(acks);
    }

    /** The semaphore that counts the acknowledgements of the write about to be sent. */
    Semaphore acksForNextWrite() {
        return new Semaphore(0);
    }

    /** Waits for a quorum of acknowledgements, at most timeoutMillis. True if they came. */
    boolean awaitQuorum(Semaphore acks) throws InterruptedException {
        return acks.tryAcquire(quorum, timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
