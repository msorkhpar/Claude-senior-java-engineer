package practice;

import org.junit.jupiter.api.Test;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountRulesTest {

    /** An engine that understands rules of the form "total > N" and records that it ran. */
    static final class ComparingEngine extends AbstractScriptEngine {
        final String label;
        final List<String> evaluated;

        ComparingEngine(String label, List<String> evaluated) {
            this.label = label;
            this.evaluated = evaluated;
        }

        @Override
        public Object eval(String script, ScriptContext context) {
            evaluated.add(label);
            String[] parts = script.trim().split("\\s*>\\s*");
            long total = ((Number) context.getAttribute(parts[0])).longValue();
            return total > Long.parseLong(parts[1]);
        }

        @Override
        public Object eval(Reader reader, ScriptContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bindings createBindings() {
            return new SimpleBindings();
        }

        @Override
        public ScriptEngineFactory getFactory() {
            return null;
        }
    }

    /** A factory, as an engine library registers one, that makes engines labelled {@code label}. */
    private static ScriptEngineFactory factory(String label, List<String> evaluated) {
        return (ScriptEngineFactory) java.lang.reflect.Proxy.newProxyInstance(
                DiscountRulesTest.class.getClassLoader(),
                new Class<?>[] {ScriptEngineFactory.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScriptEngine" -> new ComparingEngine(label, evaluated);
                    case "getEngineName" -> label;
                    case "getNames", "getExtensions", "getMimeTypes" -> List.of();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "factory " + label;
                    default -> null;
                });
    }

    /** A manager that finds no engine on the class path, with the given names registered by hand. */
    private static ScriptEngineManager installed(List<String> evaluated, String... names) {
        ScriptEngineManager manager = new ScriptEngineManager(new ClassLoader(null) { });
        for (String name : names) manager.registerEngineName(name, factory(name, evaluated));
        return manager;
    }

    @Test
    void evaluatesRulesWithTheInstalledEngine() throws Exception {
        List<String> evaluated = new ArrayList<>();
        DiscountRules rules = new DiscountRules(installed(evaluated, "graal.js"), List.of("graal.js"));

        assertThat(rules.applies("total > 480", 520)).isTrue();
        assertThat(rules.applies("total > 480", 480)).isFalse();
        assertThat(evaluated).containsOnly("graal.js");
    }

    @Test
    void prefersTheEngineNamedFirst() throws Exception {
        List<String> evaluated = new ArrayList<>();
        DiscountRules rules = new DiscountRules(installed(evaluated, "rhino", "graal.js"),
                List.of("rhino", "graal.js"));

        rules.applies("total > 75", 90);

        assertThat(evaluated).containsExactly("rhino");
    }

    @Test
    void refusesToStartWhenNoConfiguredEngineIsInstalled() {
        List<String> evaluated = new ArrayList<>();
        ScriptEngineManager onlyAnUnwantedOne = installed(evaluated, "groovy");

        assertThatThrownBy(() -> new DiscountRules(onlyAnUnwantedOne, List.of("nashorn", "graal.js")))
                .isInstanceOf(DiscountRules.NoScriptEngine.class)
                .hasMessageContaining("nashorn")
                .hasMessageContaining("graal.js");
    }

    @Test
    void fallsBackWhenTheFirstEngineIsNotInstalled() throws Exception {
        List<String> evaluated = new ArrayList<>();
        DiscountRules rules = new DiscountRules(installed(evaluated, "graal.js"),
                List.of("nashorn", "js", "graal.js"));

        assertThat(rules.applies("total > 1200", 1500)).isTrue();
        assertThat(evaluated).containsExactly("graal.js");
    }
}
