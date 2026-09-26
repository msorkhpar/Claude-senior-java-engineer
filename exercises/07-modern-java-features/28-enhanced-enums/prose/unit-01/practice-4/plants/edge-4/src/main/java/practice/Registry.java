package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Registry<T> {

    private final List<String> names = new ArrayList<>();
    private final List<T> values = new ArrayList<>();

    private int indexOf(String name) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i) == name) {
                return i;
            }
        }
        return -1;
    }

    /** Adds {@code value} under {@code name}; a name already taken is refused. */
    public void register(String name, T value) {
        if (indexOf(name) >= 0) {
            throw new IllegalArgumentException("Already registered: " + name);
        }
        names.add(name);
        values.add(value);
    }

    /** The value registered under {@code name}; throws when there is none. */
    public T get(String name) {
        int i = indexOf(name);
        if (i < 0) {
            throw new NoSuchElementException("Not registered: " + name);
        }
        return values.get(i);
    }

    /** The names, in registration order. */
    public List<String> names() {
        return List.copyOf(names);
    }

    /** How many entries there are. */
    public int size() {
        return names.size();
    }
}
