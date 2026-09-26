package practice;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
public final class Ranks {
    private Ranks() {}
    private static final TreeSet<String> OUT = new TreeSet<>();
    public static NavigableSet<String> between(ConcurrentSkipListSet<String> set, String from, String to) {
        OUT.clear(); OUT.addAll(set.subSet(from, true, to, true)); return OUT;
    }
    public static List<String> ends(ConcurrentSkipListSet<String> set) {
        return set.isEmpty() ? List.of() : List.of(set.first(), set.last());
    }
}
