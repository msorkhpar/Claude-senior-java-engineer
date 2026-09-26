package practice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Groups {

    private final ConcurrentHashMap<String, List<String>> groups;

    public Groups(ConcurrentHashMap<String, List<String>> groups) {
        this.groups = groups;
    }

    /** Adds member to group, creating the group on its first join. */
    public void join(String group, String member) {
        throw new UnsupportedOperationException("write join");
    }

    /** The group's members in join order, as a list later joins do not change. */
    public List<String> members(String group) {
        throw new UnsupportedOperationException("write members");
    }
}
