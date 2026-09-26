package practice;

import java.util.Locale;
import java.util.Objects;

public final class Tag {

    private final String name;
    private final String key;

    public Tag(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.key = name.toLowerCase(Locale.ROOT);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        Tag that = (Tag) other;
        return that != null && key.equals(that.key);
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
