package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RetryQueueTest {

    private final RetryQueue retries = new RetryQueue(4);

    private static RetryQueue.Job job(String id, int attempts) {
        return new RetryQueue.Job(id, attempts);
    }

    private static List<String> ids(List<RetryQueue.Job> queue) {
        return queue.stream().map(RetryQueue.Job::id).toList();
    }

    @Test
    void countsTheOneJobThatGaveUp() {
        List<RetryQueue.Job> queue = new ArrayList<>(List.of(
                job("invoice-311", 1), job("invoice-312", 3), job("payout-88", 4), job("invoice-313", 0)));

        assertThat(retries.dropGivenUp(queue)).isEqualTo(1);
    }

    @Test
    void dropsNothingWhenEveryJobHasAttemptsLeft() {
        List<RetryQueue.Job> queue = new ArrayList<>(List.of(
                job("refund-5", 2), job("refund-6", 3), job("refund-7", 0)));

        assertThat(retries.dropGivenUp(queue)).isZero();
        assertThat(ids(queue)).containsExactly("refund-5", "refund-6", "refund-7");
    }

    @Test
    void dropsTwoNeighboursThatBothGaveUp() {
        List<RetryQueue.Job> queue = new ArrayList<>(List.of(
                job("export-1", 2), job("export-2", 4), job("export-3", 9), job("export-4", 1), job("export-5", 0)));

        assertThat(retries.dropGivenUp(queue)).isEqualTo(2);
        assertThat(ids(queue)).containsExactly("export-1", "export-4", "export-5");
    }

    @Test
    void dropsAJobAtTheFrontOfTheQueue() {
        List<RetryQueue.Job> queue = new ArrayList<>(List.of(
                job("sync-40", 5), job("sync-41", 2), job("sync-42", 3)));

        assertThat(retries.dropGivenUp(queue)).isEqualTo(1);
        assertThat(ids(queue)).containsExactly("sync-41", "sync-42");
    }

    @Test
    void theDispatcherSeesThePrunedQueue() {
        List<RetryQueue.Job> shared = new ArrayList<>(List.of(
                job("mail-1", 0), job("mail-2", 3), job("mail-3", 7), job("mail-4", 2)));

        retries.dropGivenUp(shared);

        assertThat(ids(shared)).containsExactly("mail-1", "mail-2", "mail-4");
    }
}
