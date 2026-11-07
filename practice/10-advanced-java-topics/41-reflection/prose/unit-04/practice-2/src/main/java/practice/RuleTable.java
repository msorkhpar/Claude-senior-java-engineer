package practice;

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
        // TODO: Return a function that calls the rule's int apply(int cents). It runs for every
        //       order line, and the service's allocation profile must not show it. A rule object
        //       that has no such method is an IllegalArgumentException, here.
        throw new IllegalStateException("TODO: prepare");
    }
}
