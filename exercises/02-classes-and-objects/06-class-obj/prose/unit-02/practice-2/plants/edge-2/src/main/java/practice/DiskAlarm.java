package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Raises an alarm when a volume's usage reaches a limit. Created with DiskAlarm.watch(...). */
public final class DiskAlarm {

    /** Receives disk readings. */
    public interface Listener {
        void onReading(String volume, int percentFull);
    }

    /**
     * The monitoring feed. A new listener is sent the latest reading of every volume at once,
     * so a dashboard opened late still shows the current state.
     */
    public static final class Feed {
        private final List<Listener> listeners = new ArrayList<>();
        private final Map<String, Integer> latest = new HashMap<>();

        public void subscribe(Listener listener) {
            listeners.add(listener);
            latest.forEach(listener::onReading);
        }

        public void publish(String volume, int percentFull) {
            latest.put(volume, percentFull);
            for (Listener l : List.copyOf(listeners)) l.onReading(volume, percentFull);
        }

        public int listeners() {
            return listeners.size();
        }
    }

    private final String volume;
    private final int limit;
    private final List<String> raised = new ArrayList<>();

    private DiskAlarm(Feed feed, String volume, int limit) {
        this.volume = volume;
        this.limit = limit;
        feed.subscribe(this::onReading);
        if (limit < 1 || limit > 99) throw new IllegalArgumentException("limit must be 1 to 99: " + limit);
    }

    /** Starts watching a volume on the feed: an alarm for every reading at or above the limit. */
    public static DiskAlarm watch(Feed feed, String volume, int limit) {
        return new DiskAlarm(feed, volume, limit);
    }

    void onReading(String volume, int percentFull) {
        if (volume.equals(this.volume) && percentFull >= limit) raised.add(volume + " at " + percentFull + "%");
    }

    /** The alarms raised so far. */
    public List<String> raised() {
        return List.copyOf(raised);
    }
}
