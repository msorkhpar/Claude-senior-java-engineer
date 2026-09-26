package practice;
import java.util.*;
public final class Resizes {
    private Resizes() {}
    public static List<Integer> capacities(int capacity, float loadFactor, int entries) {
        List<Integer> out = new ArrayList<>(); out.add(capacity);
        int threshold = (int) (capacity * loadFactor);
        for (int size = 1; size <= entries; size++) if (size > threshold) { capacity *= 2; threshold *= 2; out.add(capacity); }
        return out;
    }
    public static int initialCapacityFor(int entries) { return (int) (entries / 0.75f) + 1; }
}
