package practice;

import java.lang.management.MemoryUsage;
import java.util.OptionalDouble;

public final class MemoryReport {

    private MemoryReport() {
    }

    /** "Pool [name]: used=XMB, max=YMB", in whole mebibytes; an undefined max prints as -1. */
    public static String line(String name, MemoryUsage usage) {
        throw new UnsupportedOperationException("TODO");
    }

    /** 100 * used / max, or empty when the pool has no defined maximum. */
    public static OptionalDouble usedPercent(MemoryUsage usage) {
        throw new UnsupportedOperationException("TODO");
    }
}
