package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the notifications waiting for each user. Request threads add to it at any time; a digest
 * job runs every few minutes and mails each user what is waiting for them, in one message.
 */
public class DigestOutbox {

    /** Sends one digest message to a user. */
    public interface Mailer {
        void send(String user, List<String> notifications);
    }

    private final ConcurrentHashMap<String, List<String>> waiting = new ConcurrentHashMap<>();

    /** Called by request threads, possibly while a digest run is in progress. */
    public void add(String user, String notification) {
        waiting.merge(user, List.of(notification), (earlier, more) -> {
            List<String> all = new ArrayList<>(earlier);
            all.addAll(more);
            return List.copyOf(all);
        });
    }

    /** What is waiting right now, for the admin page. */
    public Map<String, List<String>> waiting() {
        return Map.copyOf(waiting);
    }

    /**
     * Mails every user who has notifications waiting, one message each, and returns how many
     * users it mailed. Runs while request threads keep adding.
     */
    public int sendDigests(Mailer mailer) {
        int mailed = 0;
        for (Map.Entry<String, List<String>> e : waiting.entrySet()) {
            mailer.send(e.getKey(), e.getValue());
            mailed++;
        }
        waiting.clear();
        return mailed;
    }
}
