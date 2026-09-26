package practice;

public final class Collatz {

    private Collatz() {
    }

    /** Returns the number of Collatz steps from n to 1, stopping after maxSteps. */
    public static int steps(long n, int maxSteps) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1");
        }
        long value = n;
        int steps = 0;
        do {
            if (steps >= maxSteps) {
                throw new IllegalStateException("more than " + maxSteps + " steps");
            }
            value = value % 2 == 0 ? value / 2 : 3 * value + 1;
            steps++;
        } while (value != 1);
        return steps;
    }
}
