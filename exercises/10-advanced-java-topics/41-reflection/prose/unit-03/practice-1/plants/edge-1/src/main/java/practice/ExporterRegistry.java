package practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builds report exporters from configuration: each key ("pdf", "csv", ...) names the class that
 * exports that format. The registry is built once, when the reporting service starts.
 */
public class ExporterRegistry {

    /** What every configured class must implement. */
    public interface Exporter {
        String export(String report);
    }

    /** A configuration entry the registry can't use. */
    public static class ConfigException extends RuntimeException {
        public ConfigException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final ClassLoader loader;
    private final Map<String, Supplier<Exporter>> exporters = new LinkedHashMap<>();

    /** {@code config} maps each key to the binary name of a class, as read from the config file. */
    public ExporterRegistry(Map<String, String> config, ClassLoader loader) {
        this.loader = loader;
        config.forEach((key, className) -> exporters.put(key, resolve(key, className)));
    }

    /** A new exporter for {@code key}. */
    public Exporter create(String key) {
        Supplier<Exporter> exporter = exporters.get(key);
        if (exporter == null) {
            throw new ConfigException("no exporter configured for '" + key + "'", null);
        }
        return exporter.get();
    }

    /** Creates an instance with the class's no-argument constructor. */
    static Exporter newInstance(Class<? extends Exporter> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ConfigException("cannot create " + type.getName(), e);
        }
    }

    Supplier<Exporter> resolve(String key, String className) {
        return () -> {
            try {
                return newInstance(Class.forName(className, false, loader).asSubclass(Exporter.class));
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new ConfigException("exporter '" + key + "': " + className + " is not an Exporter class", e);
            }
        };
    }
}
