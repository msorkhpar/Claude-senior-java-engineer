package practice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import practice.RetryPolicy.Job;

// The main case runs first, so a solution that keeps its count in static state starts it from zero.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RetryPolicyTest {

    /** A new Job object for every call, the way the broker reads each delivery from its message. */
    private static Job delivery(String id, String type, String payload) {
        return new Job(new String(id), new String(type), new String(payload));
    }

    @Test
    @Order(1)
    void aJobIsDeliveredAgainUntilItHasFailedTheMaximumNumberOfTimes() {
        RetryPolicy policy = new RetryPolicy(3);
        Job job = delivery("inv-8812", "send-invoice", "{\"invoice\":8812}");

        assertThat(policy.failures(job)).isZero();
        assertThat(policy.failed(job)).as("after 1 failed delivery of 3").isTrue();
        assertThat(policy.failed(job)).as("after 2 failed deliveries of 3").isTrue();
        assertThat(policy.failed(job)).as("after 3 failed deliveries of 3").isFalse();
        assertThat(policy.failures(job)).isEqualTo(3);
    }

    @Test
    @Order(2)
    void eachJobHasItsOwnCount() {
        RetryPolicy policy = new RetryPolicy(2);
        Job resize = delivery("img-301", "resize", "{\"w\":640}");
        Job notify = delivery("usr-77", "notify", "{\"channel\":\"sms\"}");

        policy.failed(resize);

        assertThat(policy.failures(notify)).as("failures of a job that never failed").isZero();
        assertThat(policy.failed(notify)).as("the first failure of the second job").isTrue();
        assertThat(policy.failures(notify)).isEqualTo(1);
        assertThat(policy.failures(resize)).isEqualTo(1);
    }

    @Test
    @Order(3)
    void aRedeliveredJobKeepsTheCountOfItsEarlierDeliveries() {
        RetryPolicy policy = new RetryPolicy(4);

        boolean again = true;
        for (int delivery = 1; delivery <= 4; delivery++) {
            again = policy.failed(delivery("exp-0415", "export", "{\"month\":\"2026-04\"}"));
        }

        assertThat(again).as("delivered again after 4 failed deliveries of 4").isFalse();
        assertThat(policy.failures(delivery("exp-0415", "export", "{\"month\":\"2026-04\"}")))
                .isEqualTo(4);
    }
}
