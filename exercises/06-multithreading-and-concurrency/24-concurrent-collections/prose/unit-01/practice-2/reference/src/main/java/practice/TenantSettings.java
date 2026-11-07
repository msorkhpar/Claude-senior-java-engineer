package practice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * One tenant's settings, read by every request thread and changed from the admin page. A tenant
 * can clear a setting, which is different from never having set it: a cleared setting overrides
 * the platform default with "nothing", a setting never set falls back to the default.
 */
public class TenantSettings {

    private final ConcurrentHashMap<String, Object> stored = new ConcurrentHashMap<>();

    /** Sets a setting; value may be null, which clears it. */
    public void set(String name, String value) {
        stored.put(name, encode(value));
    }

    /** The setting's value; null when it was cleared or never set. */
    public String value(String name) {
        Object v = stored.get(name);
        return v == null ? null : decode(v);
    }

    /** The value to use: the tenant's own if it ever set one, cleared or not, else the default. */
    public String effective(String name, String platformDefault) {
        return isSet(name) ? value(name) : platformDefault;
    }

    /** A private object no caller can pass in, so it can only mean "cleared". */
    private static final Object CLEARED = new Object();

    /** What to store for a value the tenant set, which may be null. */
    Object encode(String value) {
        return value == null ? CLEARED : value;
    }

    /** The value the tenant set, from what encode stored. */
    String decode(Object stored) {
        return stored == CLEARED ? null : (String) stored;
    }

    /** Whether the tenant ever set this setting, including setting it to null. */
    boolean isSet(String name) {
        return stored.containsKey(name);
    }
}
