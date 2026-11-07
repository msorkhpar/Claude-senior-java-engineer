package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MeteringTest {

    /** A listener that remembers what it was told. */
    static final class Alerts implements Metering.FareListener {
        final String name;
        final List<String> seen = new ArrayList<>();

        Alerts(String name) {
            this.name = name;
        }

        @Override
        public void fareChanged(String route, long cents) {
            seen.add(route + "=" + cents);
        }

        @Override
        public String toString() {
            return "alerts for " + name;
        }
    }

    private final Map<String, Integer> counts = new HashMap<>();

    @Test
    void passesEveryFareChangeOnAndCountsIt() {
        Alerts alerts = new Alerts("LIS-OPO");
        Metering.FareListener metered = Metering.metered(alerts, Metering.FareListener.class, counts);

        metered.fareChanged("LIS-OPO", 4_150);
        metered.fareChanged("LIS-FNC", 6_990);

        assertThat(alerts.seen).containsExactly("LIS-OPO=4150", "LIS-FNC=6990");
        assertThat(counts).containsEntry("fareChanged", 2);
    }

    @Test
    void anUnsubscribedListenerHearsNoMoreFares() {
        Alerts alerts = new Alerts("BER-VIE");
        Metering.FareListener metered = Metering.metered(alerts, Metering.FareListener.class, counts);
        Metering.FareBoard board = new Metering.FareBoard();
        board.subscribe(metered);
        board.publish("BER-VIE", 8_800);

        boolean removed = board.unsubscribe(metered);
        board.publish("BER-VIE", 7_700);

        assertThat(removed).isTrue();
        assertThat(alerts.seen).containsExactly("BER-VIE=8800");
    }

    @Test
    void aLogLineShowsWhichListenerIsMetered() {
        Alerts alerts = new Alerts("MAD-BCN");
        Metering.FareListener metered = Metering.metered(alerts, Metering.FareListener.class, counts);

        String line = "subscribed " + metered;

        assertThat(line).contains("alerts for MAD-BCN");
    }

    @Test
    void onlyFareCallsAreCounted() {
        Alerts alerts = new Alerts("ZRH-GVA");
        Metering.FareListener metered = Metering.metered(alerts, Metering.FareListener.class, counts);
        Metering.FareBoard board = new Metering.FareBoard();

        board.subscribe(metered);
        String line = "subscribed " + metered;
        Set<Metering.FareListener> seen = new HashSet<>();
        seen.add(metered);
        board.publish("ZRH-GVA", 3_300);
        board.unsubscribe(metered);

        assertThat(line).isNotEmpty();
        assertThat(counts).containsOnlyKeys("fareChanged");
        assertThat(counts).containsEntry("fareChanged", 1);
    }
}
