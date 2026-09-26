package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LengthIndex {

    private LengthIndex() {
    }

    /** Each word length mapped to its words joined with ", " in list order; keys in first-seen order. */
    public static Map<Integer, String> byLength(List<String> words) {
        Map<Integer, String> index = new LinkedHashMap<>();
        for (String word : words) {
            Integer length = word.length();
            Integer found = null;
            for (Integer key : index.keySet()) {
                if (key == length) {
                    found = key;
                }
            }
            if (found == null) {
                index.put(length, word);
            } else {
                index.put(found, index.get(found) + ", " + word);
            }
        }
        return index;
    }
}
