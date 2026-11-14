package practice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            System.setProperty("pricing.vat", "25");
            FreshShop signUpFirst = new FreshShop();
            Object joined = signUpFirst.call("Rates", "defaultTariff");
            System.out.println("sign-up first, tariff: " + on(joined, "name") + " at " + on(joined, "vatPercent") + "% VAT");
            System.out.println("sign-up first, 8000 net costs: " + on(joined, "grossCents", 8_000L));
            FreshShop pricesFirst = new FreshShop();
            Object standard = pricesFirst.loadClass("practice.Pricing$Tariff").getField("STANDARD").get(null);
            System.out.println("price list first, standard VAT: " + on(standard, "vatPercent") + "%");
            Object later = pricesFirst.call("Rates", "defaultTariff");
            System.out.println("price list first, sign-up gets standard: " + (later == standard));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class FreshShop extends ClassLoader {
        FreshShop() { super("fresh-shop", TryIt.class.getClassLoader()); }
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null && name.startsWith("practice.Pricing")) {
                    try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = in.readAllBytes();
                        c = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return c != null ? c : getParent().loadClass(name);
            }
        }
        Object call(String type, String method) throws Exception {
            return invoke(loadClass("practice.Pricing$" + type).getMethod(method), null);
        }
    }

    private static Object on(Object target, String method, Object... args) throws Exception {
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(method) && m.getParameterCount() == args.length) return invoke(m, target, args);
        }
        throw new NoSuchMethodException(method);
    }

    private static Object invoke(Method m, Object target, Object... args) throws Exception {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() instanceof ExceptionInInitializerError init ? init.getCause() : e.getCause();
            throw cause instanceof RuntimeException r ? r : new RuntimeException(cause);
        }
    }
}
