package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscalationTest {

    private final List<String> done = new ArrayList<>();

    private final Escalation desk = new Escalation(new Escalation.Actions() {
        @Override public void pageOnCall() { done.add("page"); }
        @Override public void postToChannel() { done.add("post"); }
        @Override public void openTicket() { done.add("ticket"); }
        @Override public void log() { done.add("log"); }
    });

    @Test
    void aSeverityOneIncidentPagesTheOnCallEngineer() {
        desk.report("checkout-latency", 1);

        assertThat(done).contains("page");
    }

    @Test
    void aSeverityThreeIncidentOpensATicket() {
        desk.report("disk-usage-db-replica", 3);

        assertThat(done).contains("ticket");
    }

    @Test
    void aSeverityTwoIncidentAlsoDoesWhatALessSevereOneDoes() {
        desk.report("queue-backlog", 2);

        assertThat(done).containsExactly("post", "ticket", "log");
    }

    @Test
    void theMildestSeverityOnlyWritesTheLog() {
        desk.report("cert-expiry-30d", 4);

        assertThat(done).containsExactly("log");
    }

    @Test
    void aSeverityOutsideTheScaleIsRejectedBeforeAnythingHappens() {
        for (int severity : new int[]{0, 5, -2, 11}) {
            assertThatThrownBy(() -> desk.report("smoke-test", severity))
                    .as("severity %d", severity)
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(done).isEmpty();
    }
}
