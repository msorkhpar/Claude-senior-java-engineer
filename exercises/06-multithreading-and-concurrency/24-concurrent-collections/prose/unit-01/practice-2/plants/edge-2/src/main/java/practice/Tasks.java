package practice;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Tasks {

    private final Deque<String> line = new ArrayDeque<>();
    private int urgentCount;

    /** Puts a task at the back of the line. */
    public void add(String task) {
        line.addLast(task);
    }

    /** Puts a task at the front of the line. */
    public void urgent(String task) {
        java.util.List<String> head = new java.util.ArrayList<>();
        for (int i = 0; i < urgentCount; i++) {
            head.add(line.pollFirst());
        }
        head.add(task);
        for (int i = head.size() - 1; i >= 0; i--) {
            line.addFirst(head.get(i));
        }
        urgentCount++;
    }

    /** Removes and returns the front task, or null when the line is empty. */
    public String next() {
        if (urgentCount > 0) {
            urgentCount--;
        }
        return line.pollFirst();
    }

    /** The back task, not removed, or null when the line is empty. */
    public String last() {
        return line.peekLast();
    }
}
