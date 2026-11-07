package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.RetryPolicy.Action.DONE;
import static practice.RetryPolicy.Action.GIVE_UP;
import static practice.RetryPolicy.Action.RETRY_LATER;
import static practice.RetryPolicy.Action.RETRY_NOW;
import static practice.RetryPolicy.Action.UNEXPECTED;

class RetryPolicyTest {

    private final RetryPolicy policy = new RetryPolicy();

    @Test
    void decidesTheCommonStatuses() {
        assertThat(policy.decide(201)).isEqualTo(DONE);
        assertThat(policy.decide(404)).isEqualTo(GIVE_UP);
        assertThat(policy.decide(422)).isEqualTo(GIVE_UP);
        assertThat(policy.decide(502)).isEqualTo(RETRY_NOW);
        assertThat(policy.decide(304)).isEqualTo(UNEXPECTED);
    }

    @Test
    void aRequestRetriesUntilItSucceeds() {
        assertThat(policy.attempts(List.of(502, 504, 200).iterator(), 5)).isEqualTo(3);
        assertThat(policy.attempts(List.of(403, 200).iterator(), 5)).isEqualTo(1);
    }

    @Test
    void throttledAndUnavailableResponsesWaitForTheServer() {
        assertThat(policy.decide(429)).isEqualTo(RETRY_LATER);
        assertThat(policy.decide(503)).isEqualTo(RETRY_LATER);
    }

    @Test
    void theFirstAndLastCodeOfEachClassAreDecidedLikeTheRest() {
        assertThat(policy.decide(200)).isEqualTo(DONE);
        assertThat(policy.decide(299)).isEqualTo(DONE);
        assertThat(policy.decide(400)).isEqualTo(GIVE_UP);
        assertThat(policy.decide(499)).isEqualTo(GIVE_UP);
        assertThat(policy.decide(500)).isEqualTo(RETRY_NOW);
        assertThat(policy.decide(599)).isEqualTo(RETRY_NOW);
    }

    @Test
    void statusesOutsideTheKnownClassesAreUnexpected() {
        assertThat(policy.decide(600)).isEqualTo(UNEXPECTED);
        assertThat(policy.decide(999)).isEqualTo(UNEXPECTED);
        assertThat(policy.decide(101)).isEqualTo(UNEXPECTED);
        assertThat(policy.decide(-1)).isEqualTo(UNEXPECTED);
    }
}
