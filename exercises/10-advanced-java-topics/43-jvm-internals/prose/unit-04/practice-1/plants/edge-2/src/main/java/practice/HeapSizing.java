package practice;

import java.util.List;
import java.util.Locale;

public final class HeapSizing {

    private final long maxHeap;
    private final long threadStack;

    private HeapSizing(long maxHeap, long threadStack) {
        this.maxHeap = maxHeap;
        this.threadStack = threadStack;
    }

    /** Reads the heap and stack flags from {@code args} for a container of {@code containerLimitBytes}. */
    public static HeapSizing parse(List<String> args, long containerLimitBytes) {
        Long xmx = null;
        Long xms = null;
        long xss = 1024L * 1024;
        double percentage = 25.0;
        boolean percentageGiven = false;
        for (String arg : args) {
            if (arg.startsWith("-Xmx")) {
                xmx = size(arg.substring(4));
            } else if (arg.startsWith("-Xms")) {
                xms = size(arg.substring(4));
            } else if (arg.startsWith("-Xss")) {
                xss = size(arg.substring(4));
            } else if (arg.startsWith("-XX:MaxRAMPercentage=")) {
                percentage = Double.parseDouble(arg.substring("-XX:MaxRAMPercentage=".length()));
                percentageGiven = true;
            }
        }
        long maxHeap = xmx != null && !percentageGiven ? xmx : (long) (containerLimitBytes * percentage / 100);
        if (xms != null && xms > maxHeap) {
            throw new IllegalArgumentException("initial heap " + xms + " is larger than the maximum " + maxHeap);
        }
        return new HeapSizing(maxHeap, xss);
    }

    private static long size(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        long unit = switch (t.isEmpty() ? ' ' : t.charAt(t.length() - 1)) {
            case 'k' -> 1024L;
            case 'm' -> 1024L * 1024;
            case 'g' -> 1024L * 1024 * 1024;
            default -> 1L;
        };
        String digits = unit == 1L ? t : t.substring(0, t.length() - 1);
        return Long.parseLong(digits) * unit;
    }

    public long maxHeapBytes() {
        return maxHeap;
    }

    public long threadStackBytes() {
        return threadStack;
    }
}
