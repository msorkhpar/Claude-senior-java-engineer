package practice;
import java.util.*;
public final class Dedupe {
    private Dedupe() {}
    public static List<String> distinct(List<String> words) {
        Set<Integer> seen = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (String w : words) if (seen.add(w.hashCode())) out.add(w);
        return out;
    }
}
