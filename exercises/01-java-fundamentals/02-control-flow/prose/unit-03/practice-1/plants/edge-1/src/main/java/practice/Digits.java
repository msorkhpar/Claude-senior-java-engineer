package practice;

public final class Digits {

    private Digits() {
    }

    /** Returns how many decimal digits n has, ignoring its sign. */
    public static int count(long n) {
        long rest = n;
        int count = 0;
        while (rest != 0) {
            rest /= 10;
            count++;
        }
        return count;
    }
}
