package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class MiniContainer {

    /** Marks the constructor the container uses. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    public @interface Inject {
    }

    /** The one instance of type, created with its dependencies on first request. */
    public <T> T resolve(Class<T> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
