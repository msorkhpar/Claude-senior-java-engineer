package practice;

public final class ReverseDigits {

    private ReverseDigits() {
    }

    /** Returns {@code x} with its decimal digits reversed, or 0 when the result does not fit in an int. */
    public static int reverse(int x) {
        long reversed = 0;
        while (x != 0) {
            reversed = reversed * 10 + x % 10;
            x /= 10;
        }
        if (reversed < Integer.MIN_VALUE || reversed > Integer.MAX_VALUE) {
            return 0;
        }
        return (int) reversed;
    }
}
