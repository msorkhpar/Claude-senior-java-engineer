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
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            result.merge(keys.get(i), values.get(i), merge);
        }
        return result;
    }
}
