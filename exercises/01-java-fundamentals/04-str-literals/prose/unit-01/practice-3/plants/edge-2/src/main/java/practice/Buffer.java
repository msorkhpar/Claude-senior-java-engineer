package practice;

public final class Buffer {

    private Buffer() {
    }

    /** Returns buffer[start..end) as a String. */
    public static String word(char[] buffer, int start, int end) {
        return new String(buffer, start, Math.max(1, end - start));
    }
}
