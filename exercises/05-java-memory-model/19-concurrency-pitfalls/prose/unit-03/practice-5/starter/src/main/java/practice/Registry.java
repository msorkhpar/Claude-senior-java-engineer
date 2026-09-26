package practice;

public final class Registry {

    private Registry() {
    }

    /** The one Registry, created lazily on first use. */
    public static Registry getInstance() {
        throw new UnsupportedOperationException("write getInstance");
    }

    /** How many Registry objects were ever constructed. */
    public static int instancesCreated() {
        throw new UnsupportedOperationException("write instancesCreated");
    }
}
