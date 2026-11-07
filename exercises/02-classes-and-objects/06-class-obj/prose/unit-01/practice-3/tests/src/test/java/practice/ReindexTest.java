package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

class ReindexTest {

    private final List<Integer> reindexed = new ArrayList<>();

    private final Reindex reindex = new Reindex(partition -> {
        reindexed.add(partition);
        return 100 + partition;
    });

    @Test
    void thereIsOneJobPerPartitionAndEachReindexesOne() throws Exception {
        List<Callable<Integer>> jobs = reindex.jobs(7);

        assertThat(jobs).hasSize(7);
        for (Callable<Integer> job : jobs) job.call();
        assertThat(reindexed).hasSize(7).allMatch(p -> p >= 0 && p < 7);
    }

    @Test
    void runAfterTheyAreBuiltEveryPartitionIsReindexed() throws Exception {
        List<Callable<Integer>> jobs = reindex.jobs(5);

        for (Callable<Integer> job : jobs) job.call();

        assertThat(reindexed).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    void aJobReindexesItsOwnPartitionWhateverTheOrderAndOnARetry() throws Exception {
        List<Callable<Integer>> jobs = reindex.jobs(5);

        for (int i = jobs.size() - 1; i >= 0; i--) jobs.get(i).call();
        int written = jobs.get(1).call();

        assertThat(reindexed).containsExactly(4, 3, 2, 1, 0, 1);
        assertThat(written).isEqualTo(101);
    }
}
