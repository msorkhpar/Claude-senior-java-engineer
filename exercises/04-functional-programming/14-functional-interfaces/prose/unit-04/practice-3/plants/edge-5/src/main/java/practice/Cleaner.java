package practice;

import java.util.List;
import java.util.stream.Collectors;

public final class Cleaner {
    private Cleaner() {
    }

    private static boolean empty(String s) {
        return s == null || s.isBlank();
    }

    public static List<String> meaningful(List<String> items) {
        return items.stream().filter(s -> !empty(s)).collect(Collectors.toList());
    }

    public static int prune(List<String> names) {
        int removed = 0;
        for (int i = 0; i < names.size(); i++) {
            if (empty(names.get(i))) {
                names.remove(i);
                removed++;
            }
        }
        return removed;
    }
}
