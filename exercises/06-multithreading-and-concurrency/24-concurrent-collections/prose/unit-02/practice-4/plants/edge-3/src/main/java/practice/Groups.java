package practice;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
public final class Groups {
    private final ConcurrentHashMap<String, List<String>> groups;
    private static final List<String> OUT = new ArrayList<>();
    public Groups(ConcurrentHashMap<String, List<String>> groups) { this.groups = groups; }
    public void join(String group, String member) { groups.computeIfAbsent(group, k -> new CopyOnWriteArrayList<>()).add(member); }
    public List<String> members(String group) {
        OUT.clear(); List<String> l = groups.get(group); if (l != null) OUT.addAll(l); return OUT;
    }
}
