package practice;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

/**
 * Evaluates the shop's discount rules, small scripts such as {@code total > 250}, with whichever
 * script engine is installed. The service builds one instance when it starts.
 */
public class DiscountRules {

    /** The service can't evaluate rules with any of the engines it was configured to use. */
    public static class NoScriptEngine extends RuntimeException {
        public NoScriptEngine(String message) {
            super(message);
        }
    }

    private final ScriptEngine engine;

    public DiscountRules(ScriptEngineManager engines, List<String> engineNames) {
        this.engine = pickEngine(engines, engineNames);
    }

    /** Whether the rule grants the discount for an order with this total. */
    public boolean applies(String rule, long orderTotal) throws ScriptException {
        Bindings bindings = engine.createBindings();
        bindings.put("total", orderTotal);
        return Boolean.TRUE.equals(engine.eval(rule, bindings));
    }

    /** The engine to evaluate rules with, chosen from {@code engineNames} in order of preference. */
    static ScriptEngine pickEngine(ScriptEngineManager engines, List<String> engineNames) {
        for (String name : engineNames) {
            ScriptEngine engine = engines.getEngineByName(name);
            if (engine != null) return engine;
        }
        throw new NoScriptEngine("no script engine installed; looked for " + engineNames);
    }
}
