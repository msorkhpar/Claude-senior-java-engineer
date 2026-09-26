package practice;

public final class Collatz {

    private Collatz() {
    }

    /** Returns the number of Collatz steps from n to 1, stopping after maxSteps. */
    public static int steps(long n, int maxSteps) {
        long value = n;
        int steps = 0;
        while (value != 1) {
            if (steps >= maxSteps) {
                throw new IllegalStateException("more than " + maxSteps + " steps");
            }
            value = value % 2 == 0 ? value / 2 : 3 * value + 1;
            steps++;
        }
        return steps;
    }
}
