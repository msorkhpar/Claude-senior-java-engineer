package practice;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
public final class Ranks {
    private Ranks() {}
    public static NavigableSet<String> between(ConcurrentSkipListSet<String> set, String from, String to) {
        TreeSet<String> out = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String s : set) if (s.compareToIgnoreCase(from) >= 0 && s.compareToIgnoreCase(to) <= 0) out.add(s);
        return out;
    }
    public static List<String> ends(ConcurrentSkipListSet<String> set) {
        List<String> all = new ArrayList<>(set);
        return all.isEmpty() ? List.of() : List.of(all.get(0), all.get(all.size() - 1));
    }
}
