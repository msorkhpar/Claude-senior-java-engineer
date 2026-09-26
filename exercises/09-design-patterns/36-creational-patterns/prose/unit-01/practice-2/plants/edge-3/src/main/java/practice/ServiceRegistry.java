package practice;

public final class ServiceRegistry {

    private static int created;
    private static ServiceRegistry lazy;

    private ServiceRegistry() {
        if (lazy != null) {
            throw new IllegalStateException("Use getInstance()");
        }
        created++;
    }

    private static final class Holder {
        private static final ServiceRegistry INSTANCE = new ServiceRegistry();
    }

    /** Returns the one registry, creating it on the first call. */
    public static ServiceRegistry getInstance() {
        if (lazy == null) {
            lazy = new ServiceRegistry();
        }
        return lazy;
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
