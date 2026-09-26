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
            throw new UnsupportedOperationException("write execute");
        }
    },
    SORT {
        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            throw new UnsupportedOperationException("write execute");
        }
    },
    DISTINCT {
        @Override
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            throw new UnsupportedOperationException("write execute");
        }
    },
    REVERSE {
        @Override
        public <T> List<T> execute(List<T> input, Predicate<T> predicate) {
            throw new UnsupportedOperationException("write execute");
        }
    };

    /** Applies this operation to {@code input}; the predicate is used by FILTER only. */
    public abstract <T> List<T> execute(List<T> input, Predicate<T> predicate);

    /** Applies this operation with a predicate that accepts everything. */
    public <T> List<T> execute(List<T> input) {
        throw new UnsupportedOperationException("write execute");
    }
}
