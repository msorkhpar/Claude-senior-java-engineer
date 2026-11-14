package practice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
            Map<String, byte[]> jar = new HashMap<>();
            jar.put(InvoicePlugin.class.getName(), bytesOf(InvoicePlugin.class));
            jar.put(ShopApiExporter.class.getName(), bytesOf(ShopApiExporter.class));
            jar.put(Money.class.getName(), withVersion(bytesOf(Money.class), "host-2.0", "plug-1.7"));
            PluginLoader loader = new PluginLoader(TryIt.class.getClassLoader(), jar, Set.of(TryIt.class.getName() + "$ShopApi"));
            Object plugin = loader.loadClass(InvoicePlugin.class.getName()).getDeclaredConstructor().newInstance();
            System.out.println("plugin class loaded by: " + plugin.getClass().getClassLoader().getName());
            System.out.println("usable through the API: " + (plugin instanceof ShopApiExporter));
            if (plugin instanceof ShopApiExporter exporter) System.out.println("export: " + exporter.export("march-invoices"));
            System.out.println("server's own Money: " + Money.version());
            System.out.println("JDK String from server: " + (loader.loadClass("java.lang.String") == String.class));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    public interface ShopApiExporter {
        String export(String report);
    }
    public static class Money {
        public static String version() { return "host-2.0"; }
    }
    public static class InvoicePlugin implements ShopApiExporter {
        @Override public String export(String report) { return report + ".pdf with money lib " + Money.version(); }
    }

    private static byte[] bytesOf(Class<?> c) throws IOException {
        String resource = c.getName().substring(c.getPackageName().length() + 1) + ".class";
        try (InputStream in = c.getResourceAsStream(resource)) {
            return in.readAllBytes();
        }
    }

    private static byte[] withVersion(byte[] classFile, String from, String to) {
        byte[] a = from.getBytes(StandardCharsets.UTF_8);
        byte[] b = to.getBytes(StandardCharsets.UTF_8);
        byte[] out = classFile.clone();
        for (int i = 0; i + a.length <= out.length; i++) {
            boolean match = true;
            for (int j = 0; j < a.length && match; j++) match = out[i + j] == a[j];
            if (match) System.arraycopy(b, 0, out, i, b.length);
        }
        return out;
    }
}
