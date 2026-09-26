package practice;

public final class Sentinel {

    private Sentinel() {
    }

    /** Adds the readings up to the first 0, skipping negative ones. */
    public static int total(int[] readings) {
        int total = 0;
        for (int reading : readings) {
            if (reading <= 0) {
                break;
            }
            total += reading;
        }
        return total;
    }
}
