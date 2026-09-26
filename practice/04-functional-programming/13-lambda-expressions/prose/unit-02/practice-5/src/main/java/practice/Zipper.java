package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

public final class Zipper {

    private Zipper() {
    }

    /** keys.get(i) paired with values.get(i); a repeated key is merged as merge(existing, incoming). */
    public static Map<String, Integer> zip(List<String> keys, List<Integer> values, BinaryOperator<Integer> merge) {
        throw new UnsupportedOperationException("write zip");
    }
}
