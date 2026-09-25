package practice;

public final class ReverseDigits {

    private ReverseDigits() {
    }

    /** Returns {@code x} with its decimal digits reversed, or 0 when the result does not fit in an int. */
    public static int reverse(int x) {
        int reversed = 0;
        while (x != 0) {
            reversed = reversed * 10 + x % 10;
            x /= 10;
        }
        return reversed;
    }
}
