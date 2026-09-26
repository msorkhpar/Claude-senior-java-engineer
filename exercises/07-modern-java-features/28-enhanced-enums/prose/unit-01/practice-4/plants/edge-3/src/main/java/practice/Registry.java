package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Registry<T> {

    private final Map<String, T> entries = new HashMap<>();

    /** Adds {@code value} under {@code name}; a name already taken is refused. */
    public void register(String name, T value) {
        if (entries.containsKey(name)) {
            throw new IllegalArgumentException("Already registered: " + name);
        }
        entries.put(name, value);
    }

    /** The value registered under {@code name}; throws when there is none. */
    public T get(String name) {
        T value = entries.get(name);
        if (value == null) {
            throw new NoSuchElementException("Not registered: " + name);
        }
        return value;
    }

    /** The names, in registration order. */
    public List<String> names() {
        return List.copyOf(entries.keySet());
    }

    /** How many entries there are. */
    public int size() {
        return entries.size();
    }
}
