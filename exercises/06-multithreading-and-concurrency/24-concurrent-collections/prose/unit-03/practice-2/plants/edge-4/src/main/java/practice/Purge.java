package practice;
import java.util.List;
public final class Purge {
    private Purge() {}
    public static int removeAll(List<String> list, String target) {
        int before = list.size(); list.removeIf(target::equalsIgnoreCase); return before - list.size();
    }
}
