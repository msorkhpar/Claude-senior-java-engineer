package practice;
import java.util.*;
public final class Dedupe {
    private Dedupe() {}
    private static final List<String> OUT = new ArrayList<>();
    public static List<String> distinct(List<String> words) {
        OUT.clear();
        OUT.addAll(new LinkedHashSet<>(words));
        return OUT;
    }
}
