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
        if (items.stream().noneMatch(Cleaner::empty)) {
            return items;
        }
        return items.stream().filter(s -> !empty(s)).collect(Collectors.toList());
    }

    public static int prune(List<String> names) {
        int before = names.size();
        names.removeIf(Cleaner::empty);
        return before - names.size();
    }
}
