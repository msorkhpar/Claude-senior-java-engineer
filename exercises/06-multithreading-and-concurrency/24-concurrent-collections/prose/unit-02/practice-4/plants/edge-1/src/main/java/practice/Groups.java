package practice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Groups {

    private final ConcurrentHashMap<String, List<String>> groups;

    public Groups(ConcurrentHashMap<String, List<String>> groups) {
        this.groups = groups;
    }

    /** Adds member to group, creating the group on its first join. */
    public void join(String group, String member) {
        List<String> list = groups.get(group);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
            groups.put(group, list);
        }
        list.add(member);
    }

    /** The group's members in join order, as a list later joins do not change. */
    public List<String> members(String group) {
        List<String> list = groups.get(group);
        return list == null ? List.of() : List.copyOf(list);
    }
}
