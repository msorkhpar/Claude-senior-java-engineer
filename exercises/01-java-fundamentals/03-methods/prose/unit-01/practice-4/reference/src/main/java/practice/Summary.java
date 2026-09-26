package practice;

import java.util.List;

public final class Summary {

    private Summary() {
    }

    /** Summarises the items, or says there is nothing to process. */
    public static String summary(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "nothing to process";
        }
        return items.size() + (items.size() == 1 ? " item: " : " items: ") + String.join(", ", items);
    }
}
