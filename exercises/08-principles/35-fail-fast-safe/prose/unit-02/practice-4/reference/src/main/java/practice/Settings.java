package practice;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Settings shared by many threads; a setting may be present with no value. */
public class Settings {

    private final ConcurrentHashMap<String, Optional<String>> values = new ConcurrentHashMap<>();

    public void set(String key, String valueOrNull) {
        values.put(key, Optional.ofNullable(valueOrNull));
    }

    public void unset(String key) {
        values.remove(key);
    }

    public boolean isSet(String key) {
        return values.containsKey(key);
    }

    public Optional<String> get(String key) {
        return values.getOrDefault(key, Optional.empty());
    }
}
