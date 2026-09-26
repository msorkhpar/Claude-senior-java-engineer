package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A roster that hands out either a frozen snapshot or a read-only live view of its names. */
public class Roster {

    private final List<String> names = new ArrayList<>();

    public void add(String name) {
        names.add(name);
    }

    public List<String> snapshot() {
        throw new UnsupportedOperationException("write snapshot");
    }

    public List<String> liveView() {
        throw new UnsupportedOperationException("write liveView");
    }
}
