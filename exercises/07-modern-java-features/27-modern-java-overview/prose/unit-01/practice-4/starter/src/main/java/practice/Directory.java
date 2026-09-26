package practice;

import java.util.Map;
import java.util.Optional;

public final class Directory {

    public Directory(Map<Integer, String> names, Map<String, String> cities) {
    }

    /** The user's name, or empty. */
    public Optional<String> name(int id) {
        throw new UnsupportedOperationException("write name");
    }

    /** The city of the user's name, or empty. */
    public Optional<String> city(int id) {
        throw new UnsupportedOperationException("write city");
    }

    /** The upper-cased name when longer than 3 characters, else GUEST. */
    public String badge(int id) {
        throw new UnsupportedOperationException("write badge");
    }
}
