package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventLog {

    private final CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();

    /** Records one event. */
    public void log(String event) {
        throw new UnsupportedOperationException("write log");
    }

    /** Returns an immutable snapshot of the events so far, oldest first. */
    public List<String> events() {
        throw new UnsupportedOperationException("write events");
    }

    /** Forgets every event. */
    public void clear() {
        throw new UnsupportedOperationException("write clear");
    }
}
