package practice;

import java.util.Locale;
import java.util.Objects;

public final class Tag {

    private final String name;
    private final String key;

    public Tag(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.key = name.toLowerCase();
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Tag that && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Tag[" + name + "]";
    }
}
