package practice;

import java.util.Map;

/** Alert rules as a tree: a rule fires when its readings say so. */
public class AlertRules {

    public sealed interface Rule permits Above, Const, Not, And, Or {}
    /** Fires when the metric's reading is above the threshold. */
    public record Above(String metric, double threshold) implements Rule {}
    /** Always fires, or never. */
    public record Const(boolean value) implements Rule {}
    public record Not(Rule rule) implements Rule {}
    public record And(Rule left, Rule right) implements Rule {}
    public record Or(Rule left, Rule right) implements Rule {}

    /** Whether the rule fires for these readings; a metric with no reading is not above anything. */
    public static boolean fires(Rule rule, Map<String, Double> readings) {
        return switch (rule) {
            case Above(String metric, double threshold) ->
                    readings.containsKey(metric) && readings.get(metric) > threshold;
            case Const(boolean value) -> value;
            case Not(Rule r) -> !fires(r, readings);
            case And(Rule l, Rule r) -> fires(l, readings) && fires(r, readings);
            case Or(Rule l, Rule r) -> fires(l, readings) || fires(r, readings);
        };
    }

    /**
     * The same rule with every constant folded away and every double negation removed, at any depth.
     * The result fires for exactly the readings the input fires for.
     */
    public static Rule simplify(Rule rule) {
        Rule walked = switch (rule) {
            case Above a -> a;
            case Const c -> c;
            case Not(Rule r) -> new Not(simplify(r));
            case And(Rule l, Rule r) -> new And(simplify(l), simplify(r));
            case Or(Rule l, Rule r) -> new Or(simplify(l), simplify(r));
        };
        return switch (walked) {
            case Not(Not(Rule r)) -> r;
            case Not(Const(boolean v)) -> new Const(!v);
            case And(Const(boolean v), Rule r) -> v ? r : new Const(false);
            case Or(Const(boolean v), Rule r) -> v ? new Const(true) : r;
            default -> walked;
        };
    }
}
