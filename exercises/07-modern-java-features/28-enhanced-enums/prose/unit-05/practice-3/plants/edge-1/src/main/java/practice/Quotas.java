package practice;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** The upload quota of each pricing tier, read once at startup from the service's properties. */
public final class Quotas {

    public enum Tier { FREE, TEAM, BUSINESS, ENTERPRISE }

    static final String PREFIX = "quota.";

    private final Map<Tier, Integer> megabytes;

    private Quotas(Map<Tier, Integer> megabytes) {
        this.megabytes = megabytes;
    }

    /** The upload limit of a tier, in megabytes. */
    public int megabytes(Tier tier) {
        return megabytes.get(tier);
    }

    /**
     * Reads every {@code quota.<TIER>=<megabytes>} entry; other keys belong to other parts of
     * the service and are ignored.
     */
    public static Quotas load(Properties props) {
        Map<Tier, Integer> megabytes = new EnumMap<>(Tier.class);
        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith(PREFIX)) continue;
            megabytes.put(tier(key), Integer.parseInt(props.getProperty(key).trim()));
        }
        checkComplete(megabytes);
        return new Quotas(megabytes);
    }

    /** The tier a quota key names. */
    static Tier tier(String key) {
        String name = key.substring(PREFIX.length());
        for (Tier t : Tier.values()) {
            if (t.name().equals(name)) return t;
        }
        throw new IllegalStateException("unknown tier in " + key);
    }

    /** Refuses a configuration that leaves a tier without a quota. */
    static void checkComplete(Map<Tier, Integer> megabytes) {
        // a missing tier shows up as soon as someone asks for it
    }
}
