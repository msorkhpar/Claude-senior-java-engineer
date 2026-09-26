package practice;
import java.util.*;
import java.util.function.Function;
public final class Wrapped {
    private Wrapped() {}
    public static <T, R> List<R> mapAll(List<T> syncList, Function<? super T, ? extends R> f) {
        List<R> out = new ArrayList<>();
        for (T item : new ArrayList<>(syncList)) out.add(f.apply(item));
        return out;
    }
    public static <T> List<T> snapshot(List<T> syncList) { return new ArrayList<>(syncList); }
}
