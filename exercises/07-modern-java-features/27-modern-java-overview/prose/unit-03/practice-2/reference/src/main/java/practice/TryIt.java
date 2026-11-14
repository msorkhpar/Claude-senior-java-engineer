package practice;

import java.io.Reader;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            ScriptEngineManager installed = new ScriptEngineManager(new ClassLoader(null) { });
            installed.registerEngineName("rhino", factory("rhino"));
            DiscountRules rules = new DiscountRules(installed, List.of("nashorn", "rhino"));
            System.out.println("'total > 300' for 350: " + rules.applies("total > 300", 350));
            System.out.println("'total > 300' for 300: " + rules.applies("total > 300", 300));
            try {
                new DiscountRules(new ScriptEngineManager(new ClassLoader(null) { }), List.of("nashorn", "graal.js"));
            } catch (DiscountRules.NoScriptEngine none) {
                System.out.println("start-up without an engine: " + none.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A stand-in engine library: its engines read rules of the form "total > N" and say which ran. */
    static ScriptEngineFactory factory(String label) {
        return (ScriptEngineFactory) Proxy.newProxyInstance(TryIt.class.getClassLoader(),
                new Class<?>[] {ScriptEngineFactory.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getScriptEngine" -> new AbstractScriptEngine() {
                        @Override public Object eval(String script, ScriptContext context) {
                            System.out.println("(" + label + " evaluates " + script + ")");
                            String[] parts = script.trim().split("\\s*>\\s*");
                            return ((Number) context.getAttribute(parts[0])).longValue() > Long.parseLong(parts[1]);
                        }
                        @Override public Object eval(Reader reader, ScriptContext context) { throw new UnsupportedOperationException(); }
                        @Override public Bindings createBindings() { return new SimpleBindings(); }
                        @Override public ScriptEngineFactory getFactory() { return null; }
                    };
                    case "getNames", "getExtensions", "getMimeTypes" -> List.of();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> label;
                });
    }
}
