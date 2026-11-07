package practice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;

/**
 * Pricing rules named in configuration. A rule is any object with a public method
 * {@code int apply(int cents)}; the table calls it for every line of every order.
 */
public class RuleTable {

    private final Map<String, IntUnaryOperator> rules = new ConcurrentHashMap<>();

    /** Registers a rule under a name. Runs at startup, once per configured rule. */
    public void add(String name, Object rule) {
        rules.put(name, prepare(rule));
    }

    /** Applies the named rule to an amount in cents. Runs millions of times a minute. */
    public int apply(String name, int cents) {
        IntUnaryOperator rule = rules.get(name);
        if (rule == null) throw new IllegalArgumentException("no rule named " + name);
        return rule.applyAsInt(cents);
    }

    /** Turns one rule object into the function apply() calls for it. */
    IntUnaryOperator prepare(Object rule) {
        java.lang.reflect.Method method;
        try {
            method = rule.getClass().getMethod("apply", int.class);
            if (method.getReturnType() != int.class) throw new NoSuchMethodException("apply returns " + method.getReturnType());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(rule.getClass().getName() + " has no int apply(int)", e);
        }
        return cents -> {
            try {
                return (int) method.invoke(rule, cents);
            } catch (java.lang.reflect.InvocationTargetException t) {
                throw new IllegalStateException(t.getCause());
            } catch (ReflectiveOperationException t) {
                throw new IllegalStateException(t);
            }
        };
    }
}
