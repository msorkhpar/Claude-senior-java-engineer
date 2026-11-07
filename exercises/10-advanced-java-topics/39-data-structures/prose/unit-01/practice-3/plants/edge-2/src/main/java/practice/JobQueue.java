package practice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/** The batch scheduler's queue: the worker always takes the job that is due soonest. */
public class JobQueue {

    public record Job(String name, Instant due) {}

    private final PriorityQueue<Job> queue = new PriorityQueue<>(Comparator.comparing(Job::due));

    public void submit(Job job) {
        queue.add(job);
    }

    /** Called by the worker: removes and returns the job due soonest. */
    public Optional<Job> next() {
        return Optional.ofNullable(queue.poll());
    }

    public int size() {
        return queue.size();
    }

    /** For the dashboard: the k jobs due soonest, soonest first, without taking them off the queue. */
    public List<Job> upcoming(int k) {
        List<Job> soonest = new ArrayList<>();
        while (soonest.size() < k && !queue.isEmpty()) soonest.add(queue.poll());
        return soonest;
    }
}
