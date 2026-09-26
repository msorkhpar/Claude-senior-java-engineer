package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class NameRegistry {

    private final Set<String> names;
    private final List<String> seen = new ArrayList<>();

    public NameRegistry(Set<String> names) {
        this.names = names;
    }

    /** Registers the name; true only for the first registration of that name. */
    public synchronized boolean register(String name) {
        for (String known : seen) {
            if (known == name) {
                return false;
            }
        }
        seen.add(name);
        names.add(name);
        return true;
    }

    /** Says whether the name is registered. */
    public boolean isRegistered(String name) {
        return names.contains(name);
    }
}
