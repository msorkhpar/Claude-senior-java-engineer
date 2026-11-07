package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardTest {

    private final List<String> searchChannel = new CopyOnWriteArrayList<>();
    private final List<String> billingChannel = new CopyOnWriteArrayList<>();

    @BeforeEach
    void emptyFeed() {
        Dashboard.AlertFeed.LISTENERS.clear();
    }

    /** Asks for collections until the referent is gone, or gives up after a bounded number of tries. */
    private static boolean collected(WeakReference<?> ref) {
        for (int i = 0; i < 40 && ref.get() != null; i++) {
            System.gc();
            byte[][] pressure = new byte[16][];
            for (int j = 0; j < pressure.length; j++) pressure[j] = new byte[1 << 20];
        }
        return ref.get() == null;
    }

    private static WeakReference<Dashboard> openAndClose(String team, Dashboard.Channel channel) {
        Dashboard dashboard = new Dashboard(team, channel);
        dashboard.open();
        return new WeakReference<>(dashboard);                  // the caller keeps no strong reference
    }

    @Test
    void forwardsOnlyItsOwnTeamsAlerts() {
        Dashboard search = new Dashboard("search", searchChannel::add);
        Dashboard billing = new Dashboard("billing", billingChannel::add);
        search.open();
        billing.open();

        Dashboard.AlertFeed.publish("search: index lag 40s");
        Dashboard.AlertFeed.publish("billing: 12 failed captures");
        Dashboard.AlertFeed.publish("searchlight: bulb out");
        Dashboard.AlertFeed.publish("search: index lag 95s");

        assertThat(searchChannel).containsExactly("search: index lag 40s", "search: index lag 95s");
        assertThat(billingChannel).containsExactly("billing: 12 failed captures");
    }

    @Test
    void aClosedDashboardCanBeCollected() {
        WeakReference<Dashboard> closed = openAndClose("search", searchChannel::add);

        assertThat(collected(closed)).as("closed dashboard collected").isTrue();
    }

    @Test
    void aClosedDashboardIsCollectedWhileItsAlertsStillReachTheChannel() {
        WeakReference<Dashboard> closed = openAndClose("billing", billingChannel::add);

        boolean gone = collected(closed);
        Dashboard.AlertFeed.publish("billing: refund queue stuck");

        assertThat(billingChannel).containsExactly("billing: refund queue stuck");
        assertThat(gone).as("closed dashboard collected").isTrue();
    }
}
