package practice;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Tasks {

    private final Deque<String> line = new ArrayDeque<>();

    /** Puts a task at the back of the line. */
    public void add(String task) {
        line.addLast(task);
    }

    /** Puts a task at the front of the line. */
    public void urgent(String task) {
        line.addFirst(task);
    }

    /** Removes and returns the front task, or null when the line is empty. */
    public String next() {
        return line.pollFirst();
    }

    /** The back task, not removed, or null when the line is empty. */
    public String last() {
        return line.peekLast();
    }
}
