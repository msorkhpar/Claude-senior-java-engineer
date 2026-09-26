package practice;

import java.util.Optional;

/** Settings shared by many threads; a setting may be present with no value. */
public class Settings {

    public void set(String key, String valueOrNull) {
        throw new UnsupportedOperationException("write set");
    }

    public void unset(String key) {
        throw new UnsupportedOperationException("write unset");
    }

    public boolean isSet(String key) {
        throw new UnsupportedOperationException("write isSet");
    }

    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("write get");
    }
}
