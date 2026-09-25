package practice;

public final class Permissions {

    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int EXECUTE = 4;

    private Permissions() {
    }

    /** Returns {@code mask} with every bit of {@code flag} set. */
    public static int grant(int mask, int flag) {
        return mask | flag;
    }

    /** Returns {@code mask} with every bit of {@code flag} cleared. */
    public static int revoke(int mask, int flag) {
        return mask & ~flag;
    }

    /** Returns {@code mask} with every bit of {@code flag} flipped. */
    public static int toggle(int mask, int flag) {
        return mask ^ flag;
    }

    /** Returns whether {@code mask} has every bit of {@code flag} set. */
    public static boolean has(int mask, int flag) {
        return (mask & flag) != 0;
    }
}
