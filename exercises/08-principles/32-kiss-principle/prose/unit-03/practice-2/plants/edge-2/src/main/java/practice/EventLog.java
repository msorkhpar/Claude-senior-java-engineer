package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventLog {

    private final CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();

    /** Records one event. */
    public void log(String event) {
        events.add(event);
    }

    /** Returns an immutable snapshot of the events so far, oldest first. */
    public List<String> events() {
        return Collections.unmodifiableList(events);
    }

    /** Forgets every event. */
    public void clear() {
        events.clear();
    }
}
