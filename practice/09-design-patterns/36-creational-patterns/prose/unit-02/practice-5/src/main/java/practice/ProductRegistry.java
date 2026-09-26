package practice;

import java.util.function.Supplier;

public final class ProductRegistry {

    /** Registers a factory under a name that is not taken yet. */
    public void register(String name, Supplier<?> factory) {
        throw new UnsupportedOperationException("write register");
    }

    /** Runs the factory registered under name. */
    public Object create(String name) {
        throw new UnsupportedOperationException("write create");
    }

    /** Runs the factory registered under name and returns its product as a T. */
    public <T> T create(String name, Class<T> type) {
        throw new UnsupportedOperationException("write create");
    }
}
