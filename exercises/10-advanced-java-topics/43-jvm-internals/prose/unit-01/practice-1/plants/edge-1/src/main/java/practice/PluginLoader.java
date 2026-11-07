package practice;

import java.util.Map;
import java.util.Set;

/**
 * The class loader the report server gives one customer's plugin.
 *
 * <p>The plugin's jar is handed over as a map from binary class name to class-file bytes. Its
 * build bundles everything the plugin was compiled against: the server's plugin API and the
 * libraries the plugin uses, in the versions the plugin was tested with.
 */
public class PluginLoader extends ClassLoader {

    private final Map<String, byte[]> bundle;
    private final Set<String> apiPrefixes;

    /**
     * @param server      the report server's own loader, which holds the API and the JDK
     * @param bundle      every class in the plugin's jar: binary name to class-file bytes
     * @param apiPrefixes the server's plugin API is every class whose binary name starts with one of these
     */
    public PluginLoader(ClassLoader server, Map<String, byte[]> bundle, Set<String> apiPrefixes) {
        super("plugin", server);
        this.bundle = Map.copyOf(bundle);
        this.apiPrefixes = Set.copyOf(apiPrefixes);
    }

    /** Whether the class belongs to the server's plugin API. */
    boolean isApi(String name) {
        return apiPrefixes.stream().anyMatch(name::startsWith);
    }

    /** Whether the plugin's jar holds a class of this name. */
    boolean inBundle(String name) {
        return bundle.containsKey(name);
    }

    /** Defines a class in this loader from the bytes in the plugin's jar. */
    Class<?> defineFromBundle(String name) {
        byte[] bytes = bundle.get(name);
        return defineClass(name, bytes, 0, bytes.length);
    }

    /**
     * Every class the plugin's code names comes through here: the plugin class the server asks for,
     * and each class the JVM resolves while running the plugin.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = inBundle(name) ? defineFromBundle(name) : getParent().loadClass(name);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
