package practice;

import java.lang.management.MemoryUsage;
import java.util.OptionalDouble;

public final class MemoryReport {

    private static final long MIB = 1024L * 1024;

    private MemoryReport() {
    }

    /** "Pool [name]: used=XMB, max=YMB", in whole mebibytes; an undefined max prints as -1. */
    public static String line(String name, MemoryUsage usage) {
        long max = usage.getMax() < 0 ? -1 : usage.getMax() / MIB;
        return "Pool [" + name + "]: used=" + usage.getUsed() / MIB + "MB, max=" + max + "MB";
    }

    /** 100 * used / max, or empty when the pool has no defined maximum. */
    public static OptionalDouble usedPercent(MemoryUsage usage) {
        if (usage.getMax() <= 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(100.0 * usage.getUsed() / usage.getCommitted());
    }
}
