package practice;

import java.util.ArrayList;
import java.util.List;

public final class EvenNumbers {

    private EvenNumbers() {
    }

    /** Returns the first count even numbers, starting from 0; empty when count is not positive. */
    public static List<Integer> first(int count) {
        if (count <= 0) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(i * 2);
        }
        return result;
    }
}
