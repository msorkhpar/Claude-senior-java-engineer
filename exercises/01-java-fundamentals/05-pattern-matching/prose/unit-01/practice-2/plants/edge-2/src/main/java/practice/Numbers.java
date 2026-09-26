package practice;

import java.util.List;

public final class Numbers {

    private Numbers() {
    }

    /** Adds up the items that are numbers. */
    public static double sum(List<Object> items) {
        double total = 0;
        for (Object item : items) {
            if (item.getClass().getSuperclass() == Number.class) {
                total += ((Number) item).doubleValue();
            }
        }
        return total;
    }
}
