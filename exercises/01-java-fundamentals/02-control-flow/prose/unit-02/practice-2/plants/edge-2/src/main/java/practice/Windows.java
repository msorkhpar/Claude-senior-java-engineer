package practice;

public final class Windows {

    private Windows() {
    }

    /** Returns the largest sum of k neighbouring readings. */
    public static int best(int[] readings, int k) {
        int best = 0;
        for (int start = 0; start <= readings.length - k; start++) {
            int sum = 0;
            for (int i = start; i < start + k; i++) {
                sum += readings[i];
            }
            if (sum > best) {
                best = sum;
            }
        }
        return best;
    }
}
