package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Failed jobs waiting for another attempt. The scheduler and the dispatcher share one list of
 * jobs; before every dispatch round the scheduler takes out the jobs that have used up their
 * attempts, so the dispatcher never sends them again.
 */
public class RetryQueue {

    /** A failed job and how many times it has been tried. */
    public record Job(String id, int attempts) {}

    private final int maxAttempts;

    public RetryQueue(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /** Whether the job has used up its attempts. */
    boolean gaveUp(Job job) {
        return job.attempts() >= maxAttempts;
    }

    /**
     * Takes every job that has used up its attempts out of the shared queue, and returns how many
     * it took out.
     */
    public int dropGivenUp(List<Job> queue) {
        List<Job> pending = new ArrayList<>(queue);   // in case the queue is unmodifiable
        pending.removeIf(this::gaveUp);
        return queue.size() - pending.size();
    }
}
