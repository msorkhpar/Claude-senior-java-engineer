package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Registry<T> {

    /** Adds {@code value} under {@code name}; a name already taken is refused. */
    public void register(String name, T value) {
        throw new UnsupportedOperationException("write register");
    }

    /** The value registered under {@code name}; throws when there is none. */
    public T get(String name) {
        throw new UnsupportedOperationException("write get");
    }

    /** The names, in registration order. */
    public List<String> names() {
        throw new UnsupportedOperationException("write names");
    }

    /** How many entries there are. */
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
