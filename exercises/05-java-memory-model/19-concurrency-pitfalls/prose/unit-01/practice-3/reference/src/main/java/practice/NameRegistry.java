package practice;

import java.util.Set;

public final class NameRegistry {

    private final Set<String> names;

    public NameRegistry(Set<String> names) {
        this.names = names;
    }

    /** Registers the name; true only for the first registration of that name. */
    public boolean register(String name) {
        // add() is one synchronized step that checks and inserts together.
        return names.add(name);
    }

    /** Says whether the name is registered. */
    public boolean isRegistered(String name) {
        return names.contains(name);
    }
}
