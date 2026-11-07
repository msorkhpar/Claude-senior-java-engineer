package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends each monitoring alert to a channel: "pager" wakes the on-call engineer, "email" and
 * "dashboard" wait for office hours, "unrouted" collects what the router doesn't recognise.
 */
public class AlertRouter {

    /** A disk on a primary host is filling up. */
    public static class DiskAlert {
        private final String host;
        private final int percentFull;

        public DiskAlert(String host, int percentFull) {
            this.host = host;
            this.percentFull = percentFull;
        }

        public String host() { return host; }

        public int percentFull() { return percentFull; }

        @Override
        public String toString() { return getClass().getSimpleName() + "(" + host + ", " + percentFull + "%)"; }
    }

    /** A disk on a read replica is filling up; a replica can be rebuilt, so it never pages. */
    public static class ReplicaDiskAlert extends DiskAlert {
        public ReplicaDiskAlert(String host, int percentFull) {
            super(host, percentFull);
        }
    }

    /** A service's 99th-percentile latency over the last five minutes. */
    public record LatencyAlert(String service, long p99Millis) {
    }

    /** Groups the alerts by channel, in the order they arrived. */
    public Map<String, List<Object>> routeAll(List<?> alerts) {
        Map<String, List<Object>> byChannel = new LinkedHashMap<>();
        for (Object alert : alerts) {
            byChannel.computeIfAbsent(channelFor(alert), c -> new ArrayList<>()).add(alert);
        }
        return byChannel;
    }

    /**
     * The channel for one alert:
     * a disk alert at 90% or more goes to "pager", below that to "email";
     * a replica's disk alert goes to "email" however full the disk is;
     * a latency alert above 2000 ms goes to "pager", otherwise to "dashboard";
     * anything else, null included, goes to "unrouted".
     */
    String channelFor(Object alert) {
        // TODO: Choose the channel for this alert, as described above. Every kind of alert the
        //       router knows must reach its own channel, whatever its numbers say.
        throw new IllegalStateException("TODO: channelFor");
    }
}
