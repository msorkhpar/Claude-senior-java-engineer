package practice;
import java.util.*;
import java.util.function.Function;
public final class Wrapped {
    private Wrapped() {}
    @SuppressWarnings("rawtypes") private static final List OUT = new ArrayList();
    @SuppressWarnings("unchecked")
    public static <T, R> List<R> mapAll(List<T> syncList, Function<? super T, ? extends R> f) {
        OUT.clear();
        synchronized (syncList) { for (T item : syncList) OUT.add(f.apply(item)); }
        return OUT;
    }
    public static <T> List<T> snapshot(List<T> syncList) { synchronized (syncList) { return new ArrayList<>(syncList); } }
}
