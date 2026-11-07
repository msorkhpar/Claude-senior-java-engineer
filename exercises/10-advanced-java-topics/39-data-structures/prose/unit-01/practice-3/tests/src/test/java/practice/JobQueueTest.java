package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobQueueTest {

    private static final Instant MIDNIGHT = Instant.parse("2026-03-09T00:00:00Z");

    /** Submits one job per entry, due that many minutes after midnight, in the order given. */
    private static JobQueue queueWith(int... minutes) {
        JobQueue queue = new JobQueue();
        for (int m : minutes) queue.submit(new JobQueue.Job("job-" + m, MIDNIGHT.plusSeconds(60L * m)));
        return queue;
    }

    private static List<String> names(List<JobQueue.Job> jobs) {
        return jobs.stream().map(JobQueue.Job::name).toList();
    }

    @Test
    void upcomingShowsTheQueuedJobs() {
        JobQueue queue = queueWith(30, 5, 12);

        assertThat(names(queue.upcoming(10))).containsExactlyInAnyOrder("job-30", "job-5", "job-12");
        assertThat(queueWith().upcoming(3)).isEmpty();
    }

    @Test
    void upcomingListsTheSoonestJobsInTheOrderTheWorkerTakesThem() {
        JobQueue queue = queueWith(50, 20, 40, 10, 35, 15, 45, 25);

        assertThat(names(queue.upcoming(3))).containsExactly("job-10", "job-15", "job-20");
        assertThat(names(queue.upcoming(5))).containsExactly("job-10", "job-15", "job-20", "job-25", "job-35");
    }

    @Test
    void lookingAtUpcomingJobsLeavesThemForTheWorker() {
        JobQueue queue = queueWith(8, 3, 6, 1);

        queue.upcoming(2);

        assertThat(queue.size()).isEqualTo(4);
        List<String> taken = new ArrayList<>();
        for (int i = 0; i < 4; i++) taken.add(queue.next().orElseThrow().name());
        assertThat(taken).containsExactly("job-1", "job-3", "job-6", "job-8");
    }
}
