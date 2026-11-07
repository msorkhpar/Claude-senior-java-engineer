package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The activity feed of a shared workspace: events in the order they happened. */
public class ActivityFeed {

    private final List<String> events = new ArrayList<>();   // oldest first

    /** Records an event as the newest one. */
    public void record(String event) {
        events.add(event);
    }

    /** Every event, oldest first: a copy. */
    public List<String> history() {
        return List.copyOf(events);
    }

    /**
     * For the dashboard, which holds on to the list it gets: every event, newest first, always
     * up to date with what was recorded later. The dashboard must not be able to change the feed.
     */
    public List<String> live() {
        return Collections.unmodifiableList(events.reversed());
    }

    /**
     * For the daily digest e-mail, built now and sent later: the newest {@code n} events (all of
     * them if there are fewer), newest first, as they are at the time of the call.
     */
    public List<String> latest(int n) {
        return List.copyOf(events.reversed().subList(0, Math.min(n, events.size())));
    }
}
