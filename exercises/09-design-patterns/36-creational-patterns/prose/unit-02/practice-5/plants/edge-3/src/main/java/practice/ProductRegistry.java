package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ProductRegistry {

    private final Map<String, Supplier<?>> factories = new HashMap<>();

    /** Registers a factory under a name that is not taken yet. */
    public void register(String name, Supplier<?> factory) {
        if (factories.putIfAbsent(name, factory) != null) {
            throw new IllegalStateException("already registered: " + name);
        }
    }

    /** Runs the factory registered under name. */
    public Object create(String name) {
        Supplier<?> factory = factories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("no factory for " + name);
        }
        return factory.get();
    }

    /** Runs the factory registered under name and returns its product as a T. */
    public <T> T create(String name, Class<T> type) {
        Object product = create(name);
        @SuppressWarnings("unchecked")
        T unchecked = (T) product;
        return unchecked;
    }
}
