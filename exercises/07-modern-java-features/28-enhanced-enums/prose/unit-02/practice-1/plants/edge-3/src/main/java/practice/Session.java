package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A user's web session. The session store serializes it after every request and reads it back on
 * the next one, possibly on another node after a restart.
 */
public final class Session implements Serializable {

    /**
     * The attributes a session can hold. This used to be an enum; it is a class now so that each
     * attribute carries the type of its value.
     */
    public static final class Attribute<T> implements Serializable {
        private static final List<Attribute<?>> ALL = new ArrayList<>();

        public static final Attribute<Long> USER_ID = new Attribute<>("userId", Long.class);
        public static final Attribute<String> CART_ID = new Attribute<>("cartId", String.class);
        public static final Attribute<Locale> LOCALE = new Attribute<>("locale", Locale.class);
        public static final Attribute<Instant> LAST_SEEN = new Attribute<>("lastSeen", Instant.class);

        private final String name;
        private final Class<T> type;

        private Attribute(String name, Class<T> type) {
            this.name = name;
            this.type = type;
            ALL.add(this);
        }

        public String name() { return name; }
        public Class<T> type() { return type; }
        @Override public String toString() { return name; }

        /** Every attribute, in the order they are declared above. */
        public static List<Attribute<?>> values() {
            return List.copyOf(ALL);
        }

        /** The attribute with this name. */
        public static Attribute<?> valueOf(String name) {
            for (Attribute<?> attribute : ALL) {
                if (attribute.name.equals(name)) return attribute;
            }
            throw new IllegalArgumentException("no attribute named " + name);
        }

        /** Decides what a deserialized attribute becomes. */
        @Serial
        private Object readResolve() throws ObjectStreamException {
            return this;
        }

        // A copy with the same name counts as the same attribute.
        @Override public boolean equals(Object other) {
            return other instanceof Attribute<?> that && that.name.equals(name);
        }

        @Override public int hashCode() {
            return name.hashCode();
        }
    }

    private final Map<Attribute<?>, Object> values = new HashMap<>();

    public <T> void put(Attribute<T> attribute, T value) {
        values.put(attribute, value);
    }

    public <T> T get(Attribute<T> attribute) {
        return attribute.type().cast(values.get(attribute));
    }

    /** What the session store writes. */
    public byte[] save() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /** What the session store reads back. */
    public static Session restore(byte[] saved) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(saved))) {
            return (Session) in.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
