package practice;

import java.lang.reflect.Field;
import java.util.function.Function;

public final class PropertyReader {

    public PropertyReader(Function<Class<?>, Field[]> scanner) {
    }

    /** The value of the named field of bean, from a per-class cache filled on first use. */
    public Object read(Object bean, String property) {
        throw new UnsupportedOperationException("TODO");
    }
}
