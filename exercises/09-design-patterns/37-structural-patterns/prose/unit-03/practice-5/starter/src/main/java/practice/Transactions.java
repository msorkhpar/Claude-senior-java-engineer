package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class Transactions {

    private Transactions() {
    }

    /** Marks an interface method that must run inside a transaction. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Transactional {
    }

    public interface TxManager {
        void begin();

        void commit();

        void rollback();
    }

    /** Returns a proxy of {@code type} that runs {@code @Transactional} methods of {@code target} in a transaction. */
    public static <T> T wrap(T target, Class<T> type, TxManager tx) {
        throw new UnsupportedOperationException("write wrap");
    }
}
