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

    /** What to store for a value the tenant set, which may be null. */
    Object encode(String value) {
        // TODO: The map underneath refuses null. Return what to store so that every value a tenant
        //       can set, null included, reads back as exactly that value.
        throw new IllegalStateException("TODO: encode");
    }

    /** The value the tenant set, from what encode stored. */
    String decode(Object stored) {
        // TODO: Turn what encode stored back into the tenant's value.
        throw new IllegalStateException("TODO: decode");
    }

    /** Whether the tenant ever set this setting, including setting it to null. */
    boolean isSet(String name) {
        // TODO: Answer whether the tenant ever set this setting, cleared or not.
        throw new IllegalStateException("TODO: isSet");
    }
}
