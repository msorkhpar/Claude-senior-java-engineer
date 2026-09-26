package practice;

public final class PluginLoader {

    /** What every plugin does. */
    public interface Plugin {
        String execute();
    }

    private PluginLoader() {
    }

    /** Creates a new instance of the named Plugin class with its no-arg constructor. */
    public static Plugin load(String className) {
        throw new UnsupportedOperationException("TODO");
    }
}
