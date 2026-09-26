package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Objects;

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
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(tx, "transaction manager must not be null");
        InvocationHandler handler = (proxy, method, args) -> {
            if (!method.isAnnotationPresent(Transactional.class)) {
                return call(method, target, args);
            }
            tx.begin();
            Object result;
            try {
                result = call(method, target, args);
            } catch (Throwable failure) {
                tx.rollback();
                throw failure;
            }
            tx.commit();
            return result;
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static Object call(java.lang.reflect.Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
