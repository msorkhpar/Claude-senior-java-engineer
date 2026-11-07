package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** A team's monitoring dashboard. While it is open, it forwards its team's alerts to the team's chat channel. */
public class Dashboard {

    /** Where a team's alerts go. */
    public interface Channel {
        void send(String text);
    }

    /** The application-wide alert feed. Listeners stay registered until someone removes them. */
    public static final class AlertFeed {
        static final List<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

        private AlertFeed() {
        }

        public static void subscribe(Consumer<String> listener) {
            LISTENERS.add(listener);
        }

        /** Alerts look like "payments: queue depth 9000". */
        public static void publish(String alert) {
            LISTENERS.forEach(listener -> listener.accept(alert));
        }
    }

    private final long[] chartData = new long[4_000_000];     // 32 MB of samples behind the charts
    private final String team;
    private final Channel channel;

    public Dashboard(String team, Channel channel) {
        this.team = team;
        this.channel = channel;
    }

    /** Whether an alert is about this dashboard's team. */
    boolean concernsTeam(String alert) {
        return alert.startsWith(team + ":");
    }

    public void open() {
        chartData[0] = System.nanoTime();
        AlertFeed.subscribe(alertForwarder());
    }

    /**
     * The listener open() registers: it sends every alert about this dashboard's team to the team's
     * channel. It stays in the feed after the dashboard is closed, so it must not keep the dashboard
     * in memory.
     */
    Consumer<String> alertForwarder() {
        Channel out = channel;
        return alert -> {
            if (concernsTeam(alert)) out.send(alert);
        };
    }
}
