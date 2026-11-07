package practice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Binds the price rules named in the shop's config, such as {@code practice.Seasonal#winterSale},
 * to the checkout. A rule is a public instance method that takes a price in cents and returns
 * the new price in cents. Rules are bound once, when the checkout starts, and applied to every
 * basket.
 */
public class PriceRules {

    /** A rule as the checkout calls it. */
    public interface Rule {
        long apply(long cents);
    }

    /** Creates one instance of {@code type} and binds its method {@code methodName} as a rule. */
    public static Rule bind(Class<?> type, String methodName) {
        MethodHandle handle = lookup(type, methodName);
        Object target = newInstance(type);
        return cents -> call(handle, target, cents);
    }

    static Object newInstance(Class<?> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("cannot create " + type.getName(), e);
        }
    }

    static MethodHandle lookup(Class<?> type, String methodName) {
        try {
            return MethodHandles.lookup()
                    .findVirtual(type, methodName, MethodType.methodType(long.class, long.class))
                    .asType(MethodType.methodType(long.class, Object.class, long.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(type.getName() + "#" + methodName + " is not a price rule", e);
        }
    }

    static long call(MethodHandle handle, Object target, long cents) {
        try {
            return (long) handle.invokeExact(target, cents);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }
}
