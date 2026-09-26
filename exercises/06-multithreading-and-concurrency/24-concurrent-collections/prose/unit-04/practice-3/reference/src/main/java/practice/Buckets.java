package practice;

public final class Buckets {

    private Buckets() {
    }

    /** The bucket HashMap uses for key in a table of capacity buckets (a power of two). */
    public static int index(Object key, int capacity) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return (h ^ (h >>> 16)) & (capacity - 1);
    }
}
