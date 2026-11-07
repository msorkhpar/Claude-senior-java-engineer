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

    /**
     * A handle for the rule method {@code methodName} of {@code type}, ready for call(...).
     */
    static MethodHandle lookup(Class<?> type, String methodName) {
        // TODO: Find the rule method, and refuse anything that isn't one with an
        //       IllegalArgumentException that names it, so a wrong config entry stops the
        //       checkout from starting. The handle you return is what call(...) receives.
        throw new IllegalStateException("TODO: lookup");
    }

    static long call(MethodHandle handle, Object target, long cents) {
        try {
            // TODO: Apply the rule through the handle from lookup(...), on target.
            throw new IllegalStateException("TODO: call");
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }
}
