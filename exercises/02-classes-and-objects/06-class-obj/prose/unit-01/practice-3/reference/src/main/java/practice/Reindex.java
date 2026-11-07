package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Splits a reindex into one job per partition. The jobs go to a pool, which runs them in whatever
 * order its threads pick them up, and runs a failed job again.
 */
public class Reindex {

    /** Reindexes one partition and returns how many documents it wrote. */
    public interface Indexer {
        int reindex(int partition);
    }

    private final Indexer indexer;

    public Reindex(Indexer indexer) {
        this.indexer = indexer;
    }

    /** The jobs for partitions 0 to partitions - 1, in that order. */
    public List<Callable<Integer>> jobs(int partitions) {
        List<Callable<Integer>> jobs = new ArrayList<>();
        for (int p = 0; p < partitions; p++) {
            int partition = p;
            jobs.add(() -> indexer.reindex(partition));
        }
        return jobs;
    }
}
