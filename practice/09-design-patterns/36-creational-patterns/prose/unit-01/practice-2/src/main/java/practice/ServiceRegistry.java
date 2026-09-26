package practice;

public final class ServiceRegistry {

    private static int created;

    private ServiceRegistry() {
        created++;
    }

    /** Returns the one registry, creating it on the first call. */
    public static ServiceRegistry getInstance() {
        throw new UnsupportedOperationException("write getInstance");
    }

    /** How many times the constructor has run. */
    public static int created() {
        return created;
    }

    /** Another static method: it must not create the instance. */
    public static String describe() {
        return "service-registry";
    }
}
