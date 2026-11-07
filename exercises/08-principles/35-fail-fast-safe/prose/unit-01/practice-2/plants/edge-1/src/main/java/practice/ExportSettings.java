package practice;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Settings of the nightly report export, read once when the exporter starts. */
public final class ExportSettings {
    /** Settings the exporter can run without, and the value it uses when the file leaves them out. */
    static final Map<String, String> DEFAULTS = Map.of("export.batch.size", "500", "export.retries", "2");
    /** Settings the exporter can't run without. */
    static final Set<String> REQUIRED = Set.of("export.dir");

    public final String dir;
    public final int batchSize;
    public final int retries;

    private ExportSettings(String dir, int batchSize, int retries) {
        this.dir = dir;
        this.batchSize = batchSize;
        this.retries = retries;
    }

    /** Reads the settings file; throws IllegalArgumentException naming the setting if the file can't be used. */
    public static ExportSettings from(Properties file) {
        checkKeys(file);
        return new ExportSettings(required(file, "export.dir"),
                intSetting(file, "export.batch.size"),
                intSetting(file, "export.retries"));
    }

    /** TODO: make sure the file holds nothing the exporter would quietly ignore. */
    static void checkKeys(Properties file) {
        Set<String> unknown = new TreeSet<>(file.stringPropertyNames());
        unknown.removeAll(DEFAULTS.keySet());
        unknown.removeAll(REQUIRED);
        if (!unknown.isEmpty())
            System.err.println("WARN ignoring unknown settings " + unknown);
    }

    /** TODO: the value of a setting the exporter can't run without. */
    static String required(Properties file, String key) {
        String value = file.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("missing setting " + key);
        return value;
    }

    /** TODO: the whole number a setting holds, or its default when the file doesn't set it. */
    static int intSetting(Properties file, String key) {
        String value = file.getProperty(key, DEFAULTS.get(key));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " is not a whole number: " + value, e);
        }
    }
}
