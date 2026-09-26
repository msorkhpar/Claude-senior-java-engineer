package practice;

import java.util.List;

public final class Sorter<T extends Comparable<T>> {

    public interface SortStrategy<T extends Comparable<T>> {
        List<T> sort(List<T> data);

        String name();
    }

    public Sorter(SortStrategy<T> strategy) {
        throw new UnsupportedOperationException("TODO");
    }

    public void setStrategy(SortStrategy<T> next) {
        throw new UnsupportedOperationException("TODO");
    }

    public SortStrategy<T> getStrategy() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<T> sort(List<T> data) {
        throw new UnsupportedOperationException("TODO");
    }
}
