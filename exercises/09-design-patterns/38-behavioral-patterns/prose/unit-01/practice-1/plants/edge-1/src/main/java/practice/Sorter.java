package practice;

import java.util.List;

public final class Sorter<T extends Comparable<T>> {

    public interface SortStrategy<T extends Comparable<T>> {
        List<T> sort(List<T> data);

        String name();
    }

    private SortStrategy<T> strategy;

    public Sorter(SortStrategy<T> strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy<T> next) {
        if (next == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public SortStrategy<T> getStrategy() {
        return strategy;
    }

    public List<T> sort(List<T> data) {
        return strategy.sort(data);
    }
}
