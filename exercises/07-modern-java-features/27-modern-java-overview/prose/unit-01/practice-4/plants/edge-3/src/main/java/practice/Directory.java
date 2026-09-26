package practice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class Directory {

    private final Map<Integer, String> names;
    private final Map<String, String> cities;

    public Directory(Map<Integer, String> names, Map<String, String> cities) {
        this.names = new HashMap<>(names);
        this.cities = new HashMap<>(cities);
    }

    /** The user's name, or empty. */
    public Optional<String> name(int id) {
        return Optional.ofNullable(names.get(id));
    }

    /** The city of the user's name, or empty. */
    public Optional<String> city(int id) {
        return name(id).flatMap(name -> Optional.ofNullable(cities.get(name)));
    }

    /** The upper-cased name when longer than 3 characters, else GUEST. */
    public String badge(int id) {
        String name = names.get(id);
        return name.length() > 3 ? name.toUpperCase(Locale.ROOT) : "GUEST";
    }
}
