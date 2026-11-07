package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRouterTest {

    private final AlertRouter router = new AlertRouter();

    @Test
    void routesEachKindOfAlert() {
        assertThat(router.channelFor(new AlertRouter.DiskAlert("db-primary-2", 96))).isEqualTo("pager");
        assertThat(router.channelFor(new AlertRouter.LatencyAlert("checkout", 3_400))).isEqualTo("pager");
        assertThat(router.channelFor(new AlertRouter.LatencyAlert("search", 180))).isEqualTo("dashboard");
        assertThat(router.channelFor("heartbeat missed: cache-3")).isEqualTo("unrouted");
        assertThat(router.channelFor(null)).isEqualTo("unrouted");
    }

    @Test
    void routesABatchByChannel() {
        AlertRouter.LatencyAlert slow = new AlertRouter.LatencyAlert("checkout", 2_600);
        AlertRouter.LatencyAlert fine = new AlertRouter.LatencyAlert("search", 95);

        Map<String, List<Object>> routed = router.routeAll(List.of(slow, fine, 42));

        assertThat(routed.get("pager")).containsExactly(slow);
        assertThat(routed.get("dashboard")).containsExactly(fine);
        assertThat(routed.get("unrouted")).containsExactly(42);
    }

    @Test
    void aDiskAlertBelowThePagingLevelGoesToEmail() {
        assertThat(router.channelFor(new AlertRouter.DiskAlert("ledger-1", 71))).isEqualTo("email");
        assertThat(router.channelFor(new AlertRouter.DiskAlert("ledger-1", 89))).isEqualTo("email");
    }

    @Test
    void aReplicaDiskAlertNeverPages() {
        assertThat(router.channelFor(new AlertRouter.ReplicaDiskAlert("db-replica-4", 98))).isEqualTo("email");
        assertThat(router.channelFor(new AlertRouter.ReplicaDiskAlert("db-replica-4", 40))).isEqualTo("email");
    }
}
