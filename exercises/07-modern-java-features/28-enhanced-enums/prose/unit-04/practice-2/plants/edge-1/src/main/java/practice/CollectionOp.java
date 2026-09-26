package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

public enum CollectionOp {
    FILTER {
        @Override
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            return input.stream().filter(predicate).toList();
        }
    },
    SORT {
        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            input.sort((a, b) -> ((Comparable<T>) a).compareTo(b));
            return input;
        }
    },
    DISTINCT {
        @Override
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            return input.stream().distinct().toList();
        }
    },
    REVERSE {
        @Override
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            Collections.reverse(input);
            return input;
        }
    };

    /** Applies this operation to {@code input}; the predicate is used by FILTER only. */
    public abstract <T> List<T> execute(List<T> input, Predicate<T> predicate);

    /** Applies this operation with a predicate that accepts everything. */
    public <T> List<T> execute(List<T> input) {
        return execute(input, t -> true);
    }
}
