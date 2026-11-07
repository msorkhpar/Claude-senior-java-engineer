package practice;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs plugins. Each plugin gets a class loader of its own and is loaded child-first, so a plugin
 * can bundle the library versions it was built with. Plugins are compiled against the host's API,
 * the nested types of this class, and their jars carry a copy of it.
 */
public final class PluginHost {

    private PluginHost() {}

    /** What the host calls on a plugin. */
    public interface Plugin {
        String run();
    }

    /** The partner API's budget, which every plugin in the process draws on. */
    public static final class Quota {
        public static final Quota INSTANCE = new Quota();
        private final AtomicInteger left = new AtomicInteger(1_000);

        private Quota() {}

        public boolean tryTake() {
            return left.getAndDecrement() > 0;
        }

        public int left() {
            return Math.max(left.get(), 0);
        }
    }

    /** Loads one plugin from its jar and builds it. */
    public static Plugin load(URL[] pluginJar, String pluginClass) throws ReflectiveOperationException {
        ClassLoader loader = new PluginLoader(pluginJar, PluginHost.class.getClassLoader());
        Object plugin = loader.loadClass(pluginClass).getConstructor().newInstance();
        return (Plugin) plugin;
    }

    /** One plugin's loader: its own classes first, then the host's. */
    static final class PluginLoader extends URLClassLoader {

        PluginLoader(URL[] pluginJar, ClassLoader host) {
            super(pluginJar, host);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    if (sharedWithHost(name)) {
                        c = getParent().loadClass(name);
                    } else {
                        try {
                            c = findClass(name);
                        } catch (ClassNotFoundException notInThePlugin) {
                            c = getParent().loadClass(name);
                        }
                    }
                }
                if (resolve) resolveClass(c);
                return c;
            }
        }

        /** Whether the class must be the host's own, never the plugin's copy. */
        static boolean sharedWithHost(String name) {
            return name.equals("practice.PluginHost") || name.startsWith("practice.PluginHost$");
        }
    }
}
