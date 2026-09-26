package practice;
import java.util.*;
public final class Tasks {
    private final Deque<String> urgent = new ArrayDeque<>();
    private final Deque<String> normal = new ArrayDeque<>();
    public void add(String t) { normal.addLast(t); }
    public void urgent(String t) { urgent.addFirst(t); }
    public String next() { String u = urgent.pollFirst(); return u != null ? u : normal.pollFirst(); }
    public String last() { return normal.peekLast(); }
}
