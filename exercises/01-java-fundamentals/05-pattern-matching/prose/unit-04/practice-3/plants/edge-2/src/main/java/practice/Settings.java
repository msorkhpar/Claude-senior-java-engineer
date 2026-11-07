package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The settings of one service, loaded from its parsed configuration file. */
public final class Settings {

    private final Map<String, String> values = new LinkedHashMap<>();
    private final List<String> unset = new ArrayList<>();

    /**
     * Loads a parsed file. The parser maps each key to a String, an Integer, a Boolean or a List of
     * those, and to null when the file names the key with no value ({@code retries:}).
     */
    public void load(Map<String, Object> parsed) {
        parsed.forEach(this::put);
    }

    /** The settings that have a value, as the text the admin page shows. */
    public Map<String, String> values() {
        return values;
    }

    /** The keys the file names without a value, in file order. */
    public List<String> unset() {
        return unset;
    }

    void put(String key, Object value) {
        switch (value) {
            case String s -> values.put(key, s);
            case Integer i -> values.put(key, Integer.toString(i));
            case Boolean b -> values.put(key, b ? "yes" : "no");
            case List<?> list -> values.put(key,
                    list.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            case null, default -> unset.add(key);
        }
    }
}
